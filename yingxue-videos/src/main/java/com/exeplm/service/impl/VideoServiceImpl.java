package com.exeplm.service.impl;

import com.exeplm.clients.CategorieClient;
import com.exeplm.clients.UserClient;
import com.exeplm.dto.VideoDetailsDto;
import com.exeplm.dto.VideoDto;
import com.exeplm.entity.User;
import com.exeplm.entity.Video;
import com.exeplm.dao.VideoDao;
import com.exeplm.service.VideoService;
import com.exeplm.utils.RedisUtil;
import com.exeplm.utils.TaoResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 视频(Video)表服务实现类
 *
 * @author makejava
 * @since 2022-07-10 21:24:39
 */
@Service("videoService")
@Transactional
public class VideoServiceImpl implements VideoService {
    @Resource
    private VideoDao videoDao;

    @Autowired
    private CategorieClient categorieClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Video queryById(Integer id) {
        return this.videoDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param video 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @Override
    public Page<Video> queryByPage(Video video, PageRequest pageRequest) {
        long total = this.videoDao.count(video);
        return new PageImpl<>(this.videoDao.queryAllByLimit(video, pageRequest), pageRequest, total);
    }

    /**
     * 新增数据
     *
     * @param video 实例对象
     * @return 实例对象
     */
    @Override
    public Video insert(Video video) {
        this.videoDao.insert(video);
        return video;
    }

    /**
     * 修改数据
     *
     * @param video 实例对象
     * @return 实例对象
     */
    @Override
    public Video update(Video video) {
        this.videoDao.update(video);
        return this.queryById(video.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.videoDao.deleteById(id) > 0;
    }

    @Override
    public List<VideoDto> queryByPage(Integer page, Integer per_page) {
        int start = (page - 1) * per_page;
        List<Video> videos = videoDao.queryByPageList(start, per_page);
        ArrayList<VideoDto> list = new ArrayList<>();
        videos.forEach(//遍历
                video -> {
                    VideoDto videoDto = new VideoDto();
                    BeanUtils.copyProperties(video,videoDto);
                    Integer categoryId = video.getCategoryId();
                    ResponseEntity<TaoResult> category = categorieClient.getCategory(categoryId);
                    Map<String,Object> data = (Map<String, Object>) category.getBody().getData();
                    videoDto.setCategory((String) data.get("name"));

                    User userUP = userClient.getUserName(video.getUid());
                    videoDto.setUploader(userUP.getName());
                    //先默认0
                    videoDto.setLikes(0);
                    //获取redis中视频的点赞数
                    Integer liked = (Integer) redisUtil.hget("videos_liked", "videos_liked_" + videoDto.getId());
                    if (!ObjectUtils.isEmpty(liked)){
                        videoDto.setLikes(liked);
                    }
                    list.add(videoDto);
                }
        );
        return list;
    }

    @Override
    public VideoDetailsDto getVideosDetails(Integer vid, String token) {
        Video video = videoDao.queryById(vid);
        VideoDetailsDto videoDetailsDto = new VideoDetailsDto();
        BeanUtils.copyProperties(video,videoDetailsDto);
        ResponseEntity<TaoResult> responseEntity = categorieClient.getCategory(video.getUid());
        Map<String,Object> data = (Map<String, Object>) responseEntity.getBody().getData();
        videoDetailsDto.setCategory((String) data.get("name"));
        User userUP = userClient.getUserName(video.getUid());
        BeanUtils.copyProperties(userUP,videoDetailsDto.getUser());
        videoDetailsDto.setLikesCount(0);
        Integer likedCount = (Integer) redisUtil.hget("videos_liked", "videos_liked_" + vid);
        if (!ObjectUtils.isEmpty(likedCount)){
            videoDetailsDto.setLikesCount(likedCount);
        }
        videoDetailsDto.setPlaysCount(0);
        Integer playsCount = (Integer) redisUtil.hget("PLAYEDVIDEOS", "PLAYED_VIDEOS_" + vid);
        if (!ObjectUtils.isEmpty(playsCount)){
            videoDetailsDto.setPlaysCount(playsCount);
        }


        if (!ObjectUtils.isEmpty(token)){
            User user = (User) redisUtil.get("session_" + token);
            //点赞
            boolean like = redisUtil.sHasKey("user_liked_videos_" + user.getId(), vid);
            videoDetailsDto.setLiked(like);
            //不喜欢
            boolean dislike = redisUtil.sHasKey("user_unliked_videos_" + user.getId(), vid);
            videoDetailsDto.setDisliked(dislike);
            //收藏
            boolean favorite = redisUtil.sHasKey("user_favoites_videos_" + user.getId(), vid);
            videoDetailsDto.setFavorite(favorite);
        }
        return videoDetailsDto;
    }
}
