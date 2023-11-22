package com.epic.pos.helper;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.epic.pos.ui.settings.tms.ProfileData;
import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.domain.entity.ConfigMapEntity;
import com.epic.pos.domain.entity.HostEntry;
import com.epic.pos.domain.entity.ProfileDataEntity;
import com.epic.pos.domain.repository.Repository;
import com.google.gson.Gson;

import java.util.Iterator;
import java.util.Map;

public class ProfileUpdateHelper {

    private final String TAG = ProfileUpdateHelper.class.getSimpleName();
    private Repository repository;
    private UpdateCompleteListener updateCompleteListener;
    private Handler handler = new Handler(Looper.getMainLooper());

    public void setUpdateCompleteListener(UpdateCompleteListener updateCompleteListener) {
        this.updateCompleteListener = updateCompleteListener;
    }

    public ProfileUpdateHelper(Repository repository) {
        this.repository = repository;
    }

    public void update(String _profileData) {
        new Thread() {
            @Override
            public void run() {
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(_profileData, ProfileData.class);
                Iterator it = profileData.getProfileDataEntity().getConfigProfile().entrySet().iterator();
                log("iterator: " + it);
                validateAndUpdate(it, profileData.getProfileDataEntity());
            }
        }.start();
    }

    private void validateAndUpdate(Iterator it, ProfileDataEntity p) {
        if (it.hasNext()) {
            try {
                Map.Entry pair = (Map.Entry) it.next();
                if (paramStartWithTableName(pair.getKey().toString())) {
                    updateInTheTable(pair.getKey().toString(), pair.getValue().toString());
                }

                validateAndUpdate(it, p);
            } catch (Exception ex) {
                log("error while updating profile.");
                if (updateCompleteListener != null) {
                    handler.post(() -> updateCompleteListener.onError());
                }
            }
        } else {
            log("all records are updated.");
            boolean mainHostExists = (p.getMainHostTids() != null && !p.getMainHostTids().isEmpty());
            boolean sharedHostExists = (p.getSharedHostTids() != null && !p.getSharedHostTids().isEmpty());

            if (mainHostExists || sharedHostExists) {
                log("primary or shared host exists");
                updateMainHostInfo(mainHostExists, sharedHostExists, p);
            } else {
                log("primary host data not exists.");
                if (updateCompleteListener != null) {
                    handler.post(() -> updateCompleteListener.onCompleted());
                }
            }
        }
    }

    private void updateMainHostInfo(boolean mainHostExists, boolean sharedHostExists, ProfileDataEntity p) {
        log("update main host info.");
        if (mainHostExists) {
            HostEntry h = p.getMainHostTids().get(0);
            repository.getMerchantById(1, merchant -> {
                repository.getTerminalByMerchant(merchant.getMerchantNumber(), terminal -> {
                    repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                        updateMainHostDb(merchant, terminal, currency, h);
                    });
                });
            });
        }

        if (sharedHostExists) {
            HostEntry h = p.getSharedHostTids().get(0);
            repository.getMerchantById(2, merchant -> {
                repository.getTerminalByMerchant(merchant.getMerchantNumber(), terminal -> {
                    repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                        updateMainHostDb(merchant, terminal, currency, h);
                    });
                });
            });
        }

        if (updateCompleteListener != null) {
            handler.post(() -> updateCompleteListener.onCompleted());
        }
    }

    private void updateMainHostDb(Merchant merchant, Terminal terminal, Currency currency, HostEntry h) {
        log("update main host db");
        //merchant data
        merchant.setRctHdr1(h.getAddressLine1());
        merchant.setRctHdr2(h.getAddressLine2());
        merchant.setRctHdr3(h.getAddressLine3());
        merchant.setCity(h.getCity());
        merchant.setContactNumber(h.getContactNumber());
        merchant.setCountry(h.getCountry());
        merchant.setDistrict(h.getDistrict());
        merchant.setEmail(h.getEmail());
        merchant.setFax(h.getFax());
        merchant.setMCC(h.getMcc());
        merchant.setMerchantID(h.getMid());
        merchant.setMobileNumber(h.getMobileNumber());
        merchant.setProvince(h.getProvince());
        merchant.setRemark(h.getRemarks());
        //currency data
        currency.setCountryCode(h.getCountryCode());
        currency.setCurrencyCode(h.getCurrencyCode());
        currency.setCurrencySymbol(h.getCurrencySymbol());
        //terminal
        terminal.setTerminalID(h.getTid());

        repository.updateMerchant(merchant, () -> {
            log("merchant updated: " + merchant.getMerchantID());
            repository.updateCurrency(currency, () -> {
                log("currency updated: " + currency.getCurrencyCode());
                repository.updateTerminal(terminal, () -> {
                    log("terminal updated: " + terminal.getTerminalID());
                });
            });
        });
    }

    private void updateInTheTable(String paramName, String value) {
        try {
            ConfigMapEntity configEntry = repository.getConfigData(paramName);
            if (configEntry != null) {
                String primaryColumn = repository.getTablePrimaryColumn(configEntry.getTableName());
                if (!TextUtils.isEmpty(primaryColumn)) {
                    if (!primaryColumn.equals(configEntry.getColumnName())) {
                        String val;
                        if (value.contains("?")) {
                            val = value.substring(0, value.length() - 1);
                        } else {
                            val = value;
                        }

                        String tableName = configEntry.getTableName();
                        String columnName = configEntry.getColumnName();
                        String rowIndex = String.valueOf(configEntry.getRowIndex());

                        //todo - for nun numerical primary keys
                        if (tableName.equals("IHT")){
                            if (rowIndex.equals("1")){
                                rowIndex = "PB";
                            }else if (rowIndex.equals("2")){
                                rowIndex = "AMEX";
                            }
                        }

                        long updateRes = repository.updateProfileData(
                                tableName,
                                columnName,
                                val,
                                primaryColumn,
                                rowIndex);

                        log("Update [Res: "+ updateRes +" | table: " + tableName + " | column: " + columnName + " | value: " + val + " | primary_column: " + primaryColumn + " | row: " + rowIndex + " ]");
                    } else {
                        log("primary key detected not updated.");
                    }
                } else {
                    log("primary column not found: " + paramName);
                }
            } else {
                log("config map record not found: " + paramName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface UpdateCompleteListener {
        void onCompleted();

        void onError();
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

    private boolean paramStartWithTableName(String param) {
        for (String tableName : Const.TABLES) {
            if (param.startsWith(tableName + "_")) {
                return true;
            }
        }

        return false;
    }


}
