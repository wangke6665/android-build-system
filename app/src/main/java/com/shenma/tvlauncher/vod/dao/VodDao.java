package com.shenma.tvlauncher.vod.dao;

import android.content.Context;

import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.vod.db.Album;

import net.tsz.afinal.FinalDb;

import java.util.List;

public class VodDao {
    private FinalDb db;

    public VodDao(Context ctx) {
        this.db = FinalDb.create(ctx, "Nshenma.db");
    }

    /**
     * 添加一个影片记录
     *
     * @param album
     */
    public void addAlbums(Album album) {
        String where = "albumType='" + album.getAlbumType() + "' and albumId='" + album.getAlbumId() + "' and typeId=" + album.getTypeId();
        //Logger.d("joychang", "where=" + where);
        if (this.db.findAllByWhere(Album.class, where).size() == 0) {
            //Logger.d("joychang", "添加=collectionTime=" + album.getCollectionTime());
            this.db.save(album);//添加
            return;
        }
        this.db.update(album, where);
        //Logger.d("joychang", "修改=collectionTime=" + album.getCollectionTime());
    }

    /**
     * 根据albumId查询Album
     *
     * @return Album
     */
    public List<Album> queryAlbumById(String albumId, int typeId) {
        String where = "albumId='" + albumId + "'" + " and typeId=" + typeId;
        //Logger.d("joychang", "查询Album条件=" + where);
        return this.db.findAllByWhere(Album.class, where);
    }

    /**
     * 根据albumId typeId查询是否追剧或者是否收藏
     *
     * @param albumId
     * @param typeId
     * @return
     */
    public Boolean queryZJById(String albumId, int typeId) {
        Boolean res = Boolean.valueOf(false);
        String where = "albumId='" + albumId + "' and typeId=" + typeId;
        //Logger.d("joychang", "查询where=" + where);
        List<Album> albums = this.db.findAllByWhere(Album.class, where);
        if (albums == null || albums.size() <= 0) {
            return res;
        }
        return Boolean.valueOf(true);
    }
//	/**
//	 * 根据albumId查询Album
//	 * @return Album
//	 */
//	public Album queryAlbumById(String albumId,int typeId){
//		String where = "albumId='"+albumId+"'"+" and typeId="+typeId;
//		return db.findWithManyToOneById(id, clazz, findClass)
//	}

    /**
     * 查询所有指定类型的Album
     *
     * @return 所有Album
     */
//    public List<Album> queryAllAppsByType(int typeId) {
//        String where = "typeid=" + typeId;
//        return db.findAllByWhere(Album.class, where);
//    }
    public List<Album> queryAllAppsByType(int typeId){
        //String where = "typeid=" + typeId;
        String where = "typeid=" + typeId + " order by time desc";//按最后观看时间查找
        //String where = "typeid=" + typeId + " order by time asc";//按最后观看时间查找
        //String where = "typeid=" + typeId + " order by id desc";//按id查找
        return db.findAllByWhere(Album.class, where);
    }


    /**
     * 删除单个app
     *
     * @param app
     */
    public void deleteApps(Album app) {
        this.db.delete(app);
    }

    /**
     * 删除指定条件的app  条件为空则全部删除	 *
     */
    public void deleteByWhere(String albumId, String albumType, int typeId) {
        String where = "albumId='" + albumId +
                "' and albumType='" + albumType +
                "' and typeId=" + typeId;
        db.deleteByWhere(Album.class, where);
    }


    /**
     * 删除指定条件的记录 条件为空则全部删除
     *
     * @param
     */
    public void deleteAllByWhere(int typeId) {
        String where = "typeId=" + typeId;
        db.deleteByWhere(Album.class, where);
    }
}
