package com.exeplm.service.impl;

import com.exeplm.clients.UserClient;
import com.exeplm.dto.CommentDto;
import com.exeplm.dto.UserDto;
import com.exeplm.entity.Comment;
import com.exeplm.dao.CommentDao;
import com.exeplm.entity.User;
import com.exeplm.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论(Comment)表服务实现类
 *
 * @author makejava
 * @since 2022-07-13 11:13:03
 */
@Service("commentService")
public class CommentServiceImpl implements CommentService {
    @Resource
    private CommentDao commentDao;

    @Autowired
    private UserClient userClient;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Comment queryById(Integer id) {
        return this.commentDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param comment 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @Override
    public Page<Comment> queryByPage(Comment comment, PageRequest pageRequest) {
        long total = this.commentDao.count(comment);
        return new PageImpl<>(this.commentDao.queryAllByLimit(comment, pageRequest), pageRequest, total);
    }

    /**
     * 新增数据
     *
     * @param comment 实例对象
     * @return 实例对象
     */
    @Override
    public Comment insert(Comment comment) {
        this.commentDao.insert(comment);
        return comment;
    }

    /**
     * 修改数据
     *
     * @param comment 实例对象
     * @return 实例对象
     */
    @Override
    public Comment update(Comment comment) {
        this.commentDao.update(comment);
        return this.queryById(comment.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.commentDao.deleteById(id) > 0;
    }

    @Override
    public List<CommentDto> queryByVidPage(Integer vid, Integer page, Integer per_page) {
        int start = (page - 1) * per_page;
        List<Comment> commentList = commentDao.queryByVidPage(vid, start, per_page);
        ArrayList<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : commentList) {
            CommentDto commentDto = new CommentDto();
            BeanUtils.copyProperties(comment,commentDto);
            Integer uid = comment.getUid();
            User user = userClient.getUserName(uid);
            //设置用户
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user,userDto);
            commentDto.setReviewer(userDto);
            //查找子评论
            List<Comment> seedComments = commentDao.queryByParentId(comment.getId(), per_page);
            ArrayList<CommentDto> seedCommentDtos = new ArrayList<>();
            for (Comment seedComment : seedComments) {
                CommentDto seedcommentDto = new CommentDto();
                BeanUtils.copyProperties(seedComment,seedcommentDto);
                Integer seeduid = seedComment.getUid();
                User seeduser = userClient.getUserName(seeduid);
                BeanUtils.copyProperties(seeduser,commentDto.getReviewer());
                seedCommentDtos.add(seedcommentDto);
            }
            //添加到父评论的子评论里
            commentDto.setSubComments(seedCommentDtos);
            //添加到列表
            commentDtos.add(commentDto);
        }
        return commentDtos;
    }
}
