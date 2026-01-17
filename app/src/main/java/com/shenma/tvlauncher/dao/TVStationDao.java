package com.shenma.tvlauncher.dao;

import android.content.Context;

import com.shenma.tvlauncher.dao.bean.TVSCollect;

import net.tsz.afinal.FinalDb;

import java.util.List;

public class TVStationDao {
    private static TVStationDao dao;
    private FinalDb db;

    private TVStationDao(Context context) {
        db = FinalDb.create(context, "Nshenma.db");
    }

    public static TVStationDao getInstance(Context context) {
        if (dao == null) {
            dao = new TVStationDao(context);
        }
        return dao;
    }

    /**
     * 添加一个电视台信息
     *
     * @param tvsi
     */
    public void addTvsi(TVSCollect tvsi) {
        db.save(tvsi);
    }

    /**
     * 查询所有用户收藏的电视台
     *
     * @return
     */
    public List<TVSCollect> queryAllTvsi() {
        return db.findAll(TVSCollect.class);
    }

    /**
     * 修改一条电视台信息
     *
     * @param tvindex
     * @param tvsi
     */
    public void updateTvsi(int tvindex, TVSCollect tvsi) {
        db.update(tvsi, "tvindex=" + tvindex);
    }

    /**
     * 删除一条电视台信息
     *
     * @param tvsi
     */
    public void deleteTvsi(TVSCollect tvsi) {
        db.delete(tvsi);
    }
}
