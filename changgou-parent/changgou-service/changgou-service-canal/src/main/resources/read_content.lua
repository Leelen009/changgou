ngx.header.content_type="application/json;charset=utf8"
local uri_args = ngx.req.get_uri_args() --获取用户请求的参数
local id = uri_args["id"] --获取请求参数中的id参数

--获取本地缓存
local cache_ngx = ngx.shared.dis_cache
--根据id获取ngx本地缓存数据
local contentCache = cache_ngx:get('content_cache_'..id)

if contentCache == "" or contentCache == nil then
    --加载Redis获取redis缓存
    local redis = require("resty.redis")
    local red = redis:new()
    red:set_timeout(2000)
    local ip = "127.0.0.1"
    local port = 6379
    red:connect(ip,port)
    local rescontent = red:get("content_"..id)

    if ngx.null == rescontent then
        --查询mysql
        local cjson = require("cjson")
        local mysql = require("resty.mysql")
        local db = mysql:new()
        db:set_timeout(1000)
        --数据库连接信息
        local props = {
            host = "127.0.0.1",
            port = 3306,
            database = "changgou_content",
            user = "root",
            password = "root"
        }
        local res = db:connect(props) -- 获取数据库连接
        local select_sql = "select url,pic from tb_content where status='1' and category_id="..id.." order by sort_order"
        res = db:query(select_sql)
        red:set("content_"..id,cjson.encode(res)) --存储到Redis key:content_id value:JSON(res)
        ngx.say(responsejson) --输出Redis缓存
        db:close()
    else
        cache_ngx:set('content_cache_'..id,rescontent,10*60) --存入nginx缓存10分钟
        ngx.say(rescontent)
    end
    red:close()
else
    ngx.say(contentCache) --输出nginx缓存
end