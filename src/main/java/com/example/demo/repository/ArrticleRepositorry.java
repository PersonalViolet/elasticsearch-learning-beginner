package com.example.demo.repository;

import com.example.demo.pojo.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArrticleRepositorry extends ElasticsearchRepository<Article, Long> {
    // 根据标题搜索
    Article findByTitle(String title);
}
