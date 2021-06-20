package com.changgou.goods.controller;

import com.changgou.goods.pojo.Album;
import com.changgou.goods.service.AlbumService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Authoe Leelen
 * @Description 相册
 */
@RestController
@RequestMapping("/album")
@CrossOrigin
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    /**
     * Album分页+条件搜索实现
     * @param album
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@RequestBody(required = false) Album album,
                                     @PathVariable int page,
                                     @PathVariable int size){
        PageInfo<Album> pageInfo = albumService.findPage(album, page, size);
        return new Result(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * Album分页搜索实现
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@PathVariable int page,
                                     @PathVariable int size){
        PageInfo<Album> pageInfo = albumService.findPage(page, size);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * 多条件搜索实现
     * @param album
     * @return
     */
    @PostMapping(value = "/search")
    public Result<List<Album>> findList(@RequestBody(required = false) Album album){
        List<Album> list = albumService.findList(album);
        return new Result<List<Album>>(true, StatusCode.OK, "查询成功！", list);
    }

    /**
     * 根据id删除实现
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable Long id){
        albumService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /**
     * 修改Album数据
     * @param album
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Album album,
                         @PathVariable Long id){
        album.setId(id);
        albumService.update(album);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    /**
     * 新增Album数据
     * @param album
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Album album){
        albumService.add(album);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    /**
     * 根据id查询Album数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Album> findById(@PathVariable Long id){
        Album album = albumService.findById(id);
        return new Result<Album>(true, StatusCode.OK, "查询成功", album);
    }

    /**
     * 查询Album全部数据
     * @return
     */
    @GetMapping
    public Result<List<Album>> findAll(){
        List<Album> list = albumService.findAll();
        return new Result<List<Album>>(true, StatusCode.OK, "查询成功", list);
    }
}
