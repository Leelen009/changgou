ngx.header.content_type="application/json;charset=utf8"
local cjson = require("cjson")
local mysql = require("resty.mysql")
local uri_args = ngx.req.get_uri_args() --获取用户请求的参数
local id = uri_args["id"] --获取请求参数中的id参数

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
db:close()

local redis = require("resty.redis")
local red = redis:new()
red:set_timeout(2000)

local ip = "127.0.0.1"
local port = 6379
red:connect(ip,port)
red:set("content_"..id,cjson.encode(res)) --key:content_id value:JSON(res)
red:close()

ngx.say("{flag:true}")

--INSERT--
