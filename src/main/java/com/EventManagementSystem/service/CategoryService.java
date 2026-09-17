package com.EventManagementSystem.service;

import java.util.List;

import com.EventManagementSystem.model.Category;

public interface CategoryService {

    Category createCategory(String name, String description);

    Category getCategoryById(long id);

    List<Category> listCategories();

    void updateCategory(long id, String name, String description);

    void deleteCategory(long id);
}
