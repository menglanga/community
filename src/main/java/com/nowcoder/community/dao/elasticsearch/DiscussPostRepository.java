package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}
