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
