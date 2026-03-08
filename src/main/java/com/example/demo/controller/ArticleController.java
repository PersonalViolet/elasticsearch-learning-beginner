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
