package com.exeplm.service.impl;

import com.exeplm.entity.Played;
import com.exeplm.dao.PlayedDao;
import com.exeplm.entity.User;
import com.exeplm.service.PlayedService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 播放历史(Played)表服务实现类
 *
 * @author makejava
 * @since 2022-07-12 18:46:18
 */
@Service("playedService")
public class PlayedServiceImpl implements PlayedService {
    @Resource
    private PlayedDao playedDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Played queryById(Integer id) {
        return this.playedDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param played 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @Override
    public Page<Played> queryByPage(Played played, PageRequest pageRequest) {
        long total = this.playedDao.count(played);
        return new PageImpl<>(this.playedDao.queryAllByLimit(played, pageRequest), pageRequest, total);
    }

    /**
     * 新增数据
     *
     * @param played 实例对象
     * @return 实例对象
     */
    @Override
    public Played insert(Played played) {
        this.playedDao.insert(played);
        return played;
    }

    /**
     * 修改数据
     *
     * @param played 实例对象
     * @return 实例对象
     */
    @Override
    public Played update(Played played) {
        this.playedDao.update(played);
        return this.queryById(played.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.playedDao.deleteById(id) > 0;
    }

    @Override
    public Played queryByUidAndVid(Integer vid, Integer userId) {
        return playedDao.queryByUidAndVid(vid,userId);
    }

    @Override
    public List<Played> queryByUid(Integer page, Integer per_page, Integer userId) {
        int start = (page - 1) * per_page;
        return playedDao.queryByUid(start,per_page,userId);
    }
}
