package com.epic.pos.domain.entity;

import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;

import java.util.List;

public class TxnData {

    private List<Transaction> transactions;
    private List<Reversal> reversals;

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Reversal> getReversals() {
        return reversals;
    }

    public void setReversals(List<Reversal> reversals) {
        this.reversals = reversals;
    }
}
