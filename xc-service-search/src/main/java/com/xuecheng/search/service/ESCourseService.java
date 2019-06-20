package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/20
 * @Description: com.xuecheng.search.service
 * @version: 1.0
 */
@Service
@Transactional
public class ESCourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ESCourseService.class);

    @Autowired
    private RestHighLevelClient client;


    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;
    @Value("${xuecheng.elasticsearch.course.search_field}")
    private String search_field;

    @Value("${xuecheng.elasticsearch.media.index}")
    private String es_media_index;
    @Value("${xuecheng.elasticsearch.media.type}")
    private String es_media_type;
    @Value("${xuecheng.elasticsearch.media.source_field}")
    private String source_media_field;


    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) throws IOException {
        QueryResult queryResult = this.searchByKeyword(page, size, courseSearchParam);
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }

    private QueryResult searchByKeyword(int page, int size, CourseSearchParam courseSearchParam) throws IOException {

        if (null == courseSearchParam) {
            courseSearchParam = new CourseSearchParam();
        }
        //创建请求查询对象 并设置索引库名称和索引类型
        SearchRequest searchRequest = new SearchRequest(es_index).types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //source要显示的字段数组
        String[] sourseFieldArr = source_field.split(",");
        //source源字段过滤
        searchSourceBuilder.fetchSource(sourseFieldArr, new String[]{});
        //设置分页
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 12;
        }
        int start = (page - 1) * size;
        searchSourceBuilder.from(start).size(size);

        //创建bool查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //获取要查询的关键字
        String keyword = courseSearchParam.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            String[] searchFieldArr = this.search_field.split(",");
            //创建multi查询条件
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword, searchFieldArr)
                    .minimumShouldMatch("70%").field("name", 10));
        }

        //获取一级分类
        String mt = courseSearchParam.getMt();
        if (StringUtils.isNotEmpty(mt)) {
            //设置过滤器条件
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", mt));
            //获取二级分类
            String st = courseSearchParam.getSt();
            if (StringUtils.isNotEmpty(st)) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("st", st));
            }
        }

        //获取难度等级
        String grade = courseSearchParam.getGrade();
        if (StringUtils.isNotEmpty(grade)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", grade));
        }

        //设置bool查询条件
        searchSourceBuilder.query(boolQueryBuilder);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        //设置searchSourceBuilder
        searchRequest.source(searchSourceBuilder);
        //执行查询
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //得到查询结果
        SearchHits hits = searchResponse.getHits();

        //创建查询结果对象
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> list = new ArrayList<>();
        //设置匹配的总记录数
        queryResult.setTotal(hits.totalHits);

        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            CoursePub coursePub = new CoursePub();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //取出 id 名称 图片 价格 难度
            String name = (String) sourceAsMap.get("name");
            //id
            String id = (String) sourceAsMap.get("id");
            coursePub.setId(id);

            //取出高亮字段
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if (null != highlightFields) {
                HighlightField nameField = highlightFields.get("name");
                if (null != nameField) {
                    Text[] fragments = nameField.getFragments();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Text str : fragments) {
                        stringBuilder.append(str.string());
                    }
                    name = stringBuilder.toString();
                }
            }

            coursePub.setName(name);
            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);

            //价格
            Double price = null;
            try {
                if (sourceAsMap.get("price") != null) {
                    price = (Double) sourceAsMap.get("price");
                }
            } catch (Exception e) {
                LOGGER.error("xuecheng search error..{}", e.getMessage());
                e.printStackTrace();
                return new QueryResult<CoursePub>();
            }
            coursePub.setPrice(price);
            //折扣价
            Double price_old = null;
            try {
                if (sourceAsMap.get("price_old") != null) {
                    price_old = (Double) sourceAsMap.get("price_old");
                }
            } catch (Exception e) {
                LOGGER.error("xuecheng search error..{}", e.getMessage());
                e.printStackTrace();
                return new QueryResult<CoursePub>();
            }
            coursePub.setPrice_old(price_old);

            list.add(coursePub);
        }

        queryResult.setList(list);

        return queryResult;
    }


    public Map<String, CoursePub> getAll(String courseId) {
        //设置索引库
        SearchRequest searchRequest = new SearchRequest(es_index);
        //设置类型
        searchRequest.types(es_type);
        //设置查询条件 根据课程id查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id", courseId));
        //取消source源字段 查询所有字段
//        searchSourceBuilder.fetchSource(new String[]{"name", "grade", "charge", "pic"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        Map<String, CoursePub> map = new HashMap<>();
        try {
            //执行搜索
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //获取搜索结果
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String courseId1 = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                CoursePub coursePub = new CoursePub();
                coursePub.setId(courseId1);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setCharge(charge);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId, coursePub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public QueryResponseResult<TeachplanMediaPub> getMedia(String[] teachplanIds) {
        //设置索引和类型
        SearchRequest searchRequest = new SearchRequest(es_media_index).types(es_media_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置source 源字段过滤
        String[] source_fields = source_media_field.split(",");
        searchSourceBuilder.fetchSource(source_fields, new String[]{});
        //设置查询条件
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id", teachplanIds));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;

        //数据列表
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        try {
            //执行搜索
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //获取搜索结果
            SearchHits hits = searchResponse.getHits();

            for (SearchHit hit : hits) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");

                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);

                //将数据加入列表
                teachplanMediaPubList.add(teachplanMediaPub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }
}
