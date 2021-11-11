package com.ttn.bootcamp.service;

import com.ttn.bootcamp.domains.Product.Category;
import com.ttn.bootcamp.domains.Product.CategoryMetadataField;
import com.ttn.bootcamp.dto.Product.CategoryDto;
import com.ttn.bootcamp.exceptions.GenericException;

import java.util.List;

public interface CategoryService {
    void checkForCategoryExist(String name) throws GenericException;

    CategoryDto addCategory(CategoryDto categoryDto) throws GenericException;

    List<Category> findAllCategory() throws GenericException;
}