# 从零带你在docker部署elasticsearch-安装IK分词器-在springboot中简单使用

## 概要

从零带你在docker部署elasticsearch，并学会在springboot的简单使用

## 前言

从零带你在`docker`部署`elasticsearch`-**安装IK分词器**-在`springboot`中简单使用。如有**错误请指正**，**感谢不尽！！！**

## 参考文档

[Docker @ Elastic](https://www.docker.elastic.co/)

[老板让我做搜索功能，我不屑：“一句 SQL 不就搞定了？” 结果老板下跪：“给你n+5，求你走吧！”](https://mp.weixin.qq.com/s/erKUvZdnx81zZV0G9sj27w)

[【ElasticSearch】IK分词器安装，配置修改，支持新增词组，中文常用mapping使用案例_elasticsearch-analysis-ik-8.15.0.zip-CSDN博客](https://blog.csdn.net/m0_74282926/article/details/150612376)

## 安装部署

### 目标

部署**elasticsearch**与**kibana**。

对新手来说，更推荐先安装一个官方的可视化工具 **Kibana**。

有了它，你可以直观地查看分析数据、对数据进行操作。

如果你要做中文搜索，必须安装 **IK 分词器**。它是专门为中文设计的，能够智能识别中文词汇的边界，把句子正确地拆分成有意义的词语。

### ⚠️ **Tip**

确保Elasticsearch版本与IK分词器的版本一致

IK分词器`github`仓库：[IK分词器](https://github.com/infinilabs/analysis-ik?tab=readme-ov-file)

### 步骤

#### 1.在[Docker @ Elastic](https://www.docker.elastic.co/)并在[IK分词器](https://github.com/infinilabs/analysis-ik?tab=readme-ov-file)寻找对应的版本

此处选择应用elasticsearch:9.1.4-amd64。`elasticsearch`、`kibana`、`IK分词器`版本须保持一致![1](.image\1.png)![2](D:\Computer Science\blog\部署elasticsearch\image\2.png)

#### 2.执行对应的命令

确保你已安装docker，打开CLI窗口，执行如下命令

- `docker pull docker.elastic.co/elasticsearch/elasticsearch:9.1.4-amd64`
- `docker pull docker.elastic.co/kibana/kibana:9.1.4-amd64`

执行结果如下（我**手误执行了命令**`docker pull docker.elastic.co/kibana/kibana:9.1.4`，忽略该命令即可）

![3](.\image\3.png)

#### 3.自定义镜像

将**IK分词器**插件安装步骤写入 `Dockerfile`，构建一个包含插件的新镜像，这样每次启动容器都自带**IK分词器插件**。

##### 自定义镜像相关教程

- [Docker Dockerfile | 菜鸟教程](https://www.runoob.com/docker/docker-dockerfile.html)
- [docker制作镜像的两种方式（保姆级教学）_docker基础镜像制作-CSDN博客](https://blog.csdn.net/weixin_51759592/article/details/156142570)

1. **创建一个目录**，例如 `es-with-ik`，并在其中创建 `Dockerfile`，Dockerfile文件不包含如`.txt`等后缀：

   ```dockerfile
   FROM docker.elastic.co/elasticsearch/elasticsearch:9.1.4-amd64
   RUN bin/elasticsearch-plugin install --batch https://get.infini.cloud/elasticsearch/analysis-ik/9.1.4
   ```

2. **构建自定义镜像**（在 `Dockerfile` 所在目录执行）：

   ```
   docker build -t elasticsearch-ik:9.1.4 .
   ```

   ![4](.\image\4.png)

完成上述操作，你将拥有两个镜像：

- 一个不带IK插件的`elasticsearch`
- 一个带IK插件的`elasticsearch`

#### 4.启动容器

1. 由于`kibana`和`Elasticsearch`部署在不同容器，想要让`kibana`连接到`Elasticsearch`需要加入到同一Docker网络中

   ```
   docker network create elastic
   ```

2. 分别执行以下命令，启动容器

   1. `elasticsearch-IK`分词器：

      ```
      docker run -d --name es-ik-amd64 --network elastic -p 9201:9200 -e discovery.type=single-node -e xpack.security.enabled=false elasticsearch-ik
      ```

      如果你想要**持久化存储**，执行以下命令

      ```
      docker run -d --name es-ik-amd64 --network elastic -p 9201:9200 -e discovery.type=single-node -e xpack.security.enabled=false -v 你选择的路径:/usr/share/elasticsearch/data elasticsearch-ik
      ```

   2. `kibana`：

      ```
      docker run -d --name kibana-amd64-9.1.4 --network elastic -p 5601:5601 -e ELASTICSEARCH_HOSTS=http://es-ik-amd64:9200 -e I18N_LOCALE=zh-CN docker.elastic.co/kibana/kibana:9.1.4-amd64
      ```

#### 5.访问路径验证成功

1. 访问[localhost:9201](http://localhost:9201/)，若成功返回如下信息，则证明Elasticsearch容器启动成功![5](.\image\5.png)
2. 访问localhost:5601，可以看到正常进入页面![6](.\image\6.png)

#### 6.测试使用

使用IK分词器时注意以下两点：

- **存储时**：用 `ik_max_word`，保证尽可能多地被切分，提升召回率
- **搜索时**：用 `ik_smart`，避免过多无意义的匹配，提升相关性

1. 在`kibana`侧边栏找到**开发工具**![7](.\image\7.png)

2. 在控制台输入以下命令并执行。(analyzer与search_analyzer指定了选用的分词器，即IK分词器)

   ```json
   PUT /article
   {
     "mappings": {
       "properties": {
         "title": {
           "type": "text",
           "analyzer": "ik_max_word",      // 索引时使用细粒度分词
           "search_analyzer": "ik_smart"   // 搜索时使用智能分词（可选，提高准确度）
         },
         "content": {
           "type": "text",
           "analyzer": "ik_max_word",      // 索引时使用细粒度分词
           "search_analyzer": "ik_smart"   // 搜索时使用智能分词
         },
         "tags": {
           "type": "keyword"                // 精确匹配，无需分词
         },
         "viewCount": {
           "type": "long"
         },
         "isPublished": {
           "type": "boolean"
         },
         "createTime": {
           "type": "date"
         }
       }
     }
   }
   ```

3. 插入数据

   ```json
   POST /article/_doc/1
   {
   "title": "人造卫星看的更真更远",
   "content": "人类使用地图前后前后兜兜转转需要靠人造卫星",
   "cover": "封面图地址",
   "tags": ["ES", "搜索"],
   "viewCount": 1000,
   "isPublished": true,
   "createTime": "2025-01-30"
   }
   ```

4. 执行在搜索结果中高亮命中的关键词的命令

   ```json
   GET /article/_search
   {
     "query": {
       "match": { "title": "人造卫星更远吗" }
     },
     "highlight": {
       "fields": { "title": {} }
     }
   }
   ```

5. 查看返回数据，数据如下

   ```json
   {
     "took": 3,
     "timed_out": false,
     "_shards": {
       "total": 1,
       "successful": 1,
       "skipped": 0,
       "failed": 0
     },
     "hits": {
       "total": {
         "value": 1,
         "relation": "eq"
       },
       "max_score": 0.5753642,
       "hits": [
         {
           "_index": "article",
           "_id": "1",
           "_score": 0.5753642,
           "_source": {
             "title": "人造卫星看的更真更远",
             "content": "人类使用地图前后前后兜兜转转需要靠人造卫星",
             "cover": "封面图地址",
             "tags": [
               "ES",
               "搜索"
             ],
             "viewCount": 1000,
             "isPublished": true,
             "createTime": "2025-01-30"
           },
           "highlight": {
             "title": [
               "<em>人造卫星</em>看的更真<em>更远</em>"
             ]
           }
         }
       ]
     }
   }
   ```

   可以看到**人造卫星**和**更远**都命中，说明**IK分词器**应用成功。

## SpringBoot中使用

对于springboot来说，引用以下坐标即可实现用java代码操作ES，该包集成了**Spring Data Elasticsearch**。你用的客户端版本最好要跟安装的 ES 服务保持一致，不然会出各种奇奇怪怪的 Bug。

[Versions :: Spring Data Elasticsearch](https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/versions.html)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
    <version>4.1.0-M2</version>
</dependency>
```

### application.yml

```yml
spring:
  elasticsearch:
    uris: http://localhost:9201
    # 若需认证（如 Elastic Cloud）
    # username: elastic
    # password: your-password
    connection-timeout: 1s
    socket-timeout: 30s
```

### 测试使用

测试代码都由AI生成。学生可在`github`进行**学生认证**领两年的**copilot会员**来辅助学习

#### Article.java

```java
package com.example.demo.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "article")
public class Article {
    @Id
    private Long id;
    private String title;
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
```

#### ArticleRepository.java

```java
package com.example.demo.repository;

import com.example.demo.pojo.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArrticleRepositorry extends ElasticsearchRepository<Article, Long> {
    // 根据标题搜索
    Article findByTitle(String title);
}
```

#### ArticleController.java

```java
package com.example.demo.controller;

import com.example.demo.pojo.Article;
import com.example.demo.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    // 保存文章
    @PostMapping
    public Article create(@RequestBody Article article) {
        return articleService.save(article);
    }
    
    // 根据 ID 查询
    @GetMapping("/{id}")
    public Article getById(@PathVariable Long id) {
        return articleService.findById(id).orElse(null);
    }
    
    // 根据标题查询
    @GetMapping("/search")
    public Article getByTitle(@RequestParam String title) {
        return articleService.findByTitle(title);
    }
    
    // 查询所有
    @GetMapping
    public Iterable<Article> getAll() {
        return articleService.findAll();
    }
    
    // 删除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        articleService.deleteById(id);
    }
}
```

#### ArticleService.java

```java
package com.example.demo.service;

import com.example.demo.pojo.Article;
import com.example.demo.repository.ArrticleRepositorry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArticleService {


    @Autowired
    private ArrticleRepositorry repository;
    
    // 保存文章
    public Article save(Article article) {
        return repository.save(article);
    }
    
    // 根据 ID 查询
    public Optional<Article> findById(Long id) {
        return repository.findById(id);
    }
    
    // 根据标题查询（使用你定义的方法）
    public Article findByTitle(String title) {
        return repository.findByTitle(title);
    }
    
    // 查询所有
    public Iterable<Article> findAll() {
        return repository.findAll();
    }
    
    // 删除
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    // 判断是否存在
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
```

#### 发出请求，接收响应数据

我使用了IDEA的Apifox插件发出请求，响应如下![8](.\image\8.png)

可以看到正确响应了数据。