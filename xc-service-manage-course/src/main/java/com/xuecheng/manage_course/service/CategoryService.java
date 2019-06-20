package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/11
 * @Description: com.xuecheng.manage_course.service
 * @version: 1.0
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public CategoryNode findCategoryList() {
        List<CategoryNode> list = categoryMapper.findAll();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
