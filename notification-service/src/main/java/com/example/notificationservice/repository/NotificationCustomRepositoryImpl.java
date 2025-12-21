package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl
        implements NotificationCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Notification> searchNotifications(
            Long userId,
            Boolean read,
            Notification.NotificationType type,
            String keyword,
            Pageable pageable
    ) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));

        if (read != null) {
            query.addCriteria(Criteria.where("read").is(read));
        }

        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }

        if (keyword != null && !keyword.isBlank()) {
            query.addCriteria(
                    new Criteria().orOperator(
                            Criteria.where("title").regex(keyword, "i"),
                            Criteria.where("content").regex(keyword, "i")
                    )
            );
        }

        long total = mongoTemplate.count(query, Notification.class);

        query.with(pageable);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Notification> data =
                mongoTemplate.find(query, Notification.class);

        return new PageImpl<>(data, pageable, total);
    }
}
