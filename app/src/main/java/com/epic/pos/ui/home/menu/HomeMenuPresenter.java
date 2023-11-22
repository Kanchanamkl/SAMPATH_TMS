package com.epic.pos.ui.home.menu;

import com.epic.pos.domain.entity.HomeMenuBean;
import com.epic.pos.ui.BasePresenter;

import java.util.ArrayList;

import javax.inject.Inject;

public class HomeMenuPresenter extends BasePresenter<HomeMenuContract.View> implements HomeMenuContract.Presenter {

    protected ArrayList<HomeMenuBean> menu;

    @Inject
    public HomeMenuPresenter() {
    }

}