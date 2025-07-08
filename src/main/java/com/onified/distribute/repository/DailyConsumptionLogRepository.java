//package com.onified.distribute.repository;
//
//import com.onified.distribute.entity.DailyConsumptionLog;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.repository.Aggregation;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface DailyConsumptionLogRepository extends MongoRepository<DailyConsumptionLog, String> {
//
//    // Find by Product and Location
//    Page<DailyConsumptionLog> findByProductIdAndLocationId(String productId, String locationId, Pageable pageable);
//
//
//
//    @Query("{'productId': ?0, 'locationId': ?1, 'consumptionDate': {'$gte': ?2, '$lt': ?3}}")
//    Optional<DailyConsumptionLog> findByProductIdAndLocationIdAndConsumptionDateBetween(
//            String productId,
//            String locationId,
//            LocalDateTime startDate,
//            LocalDateTime endDate
//    );
//
//    // Find by Location
//    Page<DailyConsumptionLog> findByLocationId(String locationId, Pageable pageable);
//
//    // Find by Product
//    Page<DailyConsumptionLog> findByProductId(String productId, Pageable pageable);
//
//    // Find by Date Range
//    Page<DailyConsumptionLog> findByConsumptionDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
//
//    // Find latest consumption for product and location
//    @Query(value = "{ 'productId': ?0, 'locationId': ?1 }", sort = "{ 'consumptionDate': -1, '_id': -1 }")
//    Optional<DailyConsumptionLog> findLatestConsumptionByProductAndLocation(String productId, String locationId);
//
//    // Check if consumption exists for specific date
//    boolean existsByProductIdAndLocationIdAndConsumptionDate(String productId, String locationId, LocalDateTime consumptionDate);
//
//    // Delete old consumption logs
//    void deleteByConsumptionDateBefore(LocalDateTime cutoffDate);
//
//    // In DailyConsumptionLogRepository
//    @Query(value = "{ 'productId': ?0, 'locationId': ?1 }",
//            sort = "{ 'consumptionDate': -1, '_id': -1 }")
//    List<DailyConsumptionLog> findLatestConsumptionByProductAndLocationWithLimit(
//            String productId,
//            String locationId,
//            Pageable pageable
//    );
//    // Aggregate consumption by month
//    @Query(value = "{'productId': ?0, 'locationId': ?1, 'consumptionDate': {$gte: ?2, $lte: ?3}}")
//    List<DailyConsumptionLog> findConsumptionForPeriod(String productId, String locationId,
//                                                       LocalDateTime startDate, LocalDateTime endDate);
//
//    @Query("{'productId': ?0, 'locationId': ?1, 'consumptionDate': {$gte: ?2}}")
//    List<DailyConsumptionLog> findRecentConsumption(String productId, String locationId, LocalDateTime cutoff);
//
//    // Sum quantity_consumed for a product and location since a cutoff time
//    @Aggregation(pipeline = {
//            "{'$match': {'productId': ?0, 'locationId': ?1, 'consumptionDate': {$gte: ?2}}}",
//            "{'$group': {'_id': null, 'totalConsumed': {'$sum': '$quantityConsumed'}}}"
//    })
//    Integer sumQuantityConsumed(String productId, String locationId, LocalDateTime cutoff);
//}


package com.onified.distribute.repository;

import com.onified.distribute.entity.DailyConsumptionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyConsumptionLogRepository extends MongoRepository<DailyConsumptionLog, String> {

    // Find by Product and Location
    Page<DailyConsumptionLog> findByProductIdAndLocationId(String productId, String locationId, Pageable pageable);

    // Sum quantity consumed between dates using aggregation
    @Aggregation(pipeline = {
            "{'$match': {'productId': ?0, 'locationId': ?1, 'consumptionDate': {'$gte': ?2, '$lte': ?3}}}",
            "{'$group': {'_id': null, 'totalConsumed': {'$sum': '$quantityConsumed'}}}"
    })
    Optional<Integer> sumQuantityConsumedBetweenDates(String productId, String locationId,
                                                      LocalDateTime startDate, LocalDateTime endDate);

    // Sum quantity consumed since a date using aggregation
    @Aggregation(pipeline = {
            "{'$match': {'productId': ?0, 'locationId': ?1, 'consumptionDate': {'$gte': ?2}}}",
            "{'$group': {'_id': null, 'totalConsumed': {'$sum': '$quantityConsumed'}}}"
    })
    Optional<Integer> sumQuantityConsumed(String productId, String locationId, LocalDateTime cutoff);

    @Query("{'productId': ?0, 'locationId': ?1, 'consumptionDate': {'$gte': ?2, '$lt': ?3}}")
    Optional<DailyConsumptionLog> findByProductIdAndLocationIdAndConsumptionDateBetween(
            String productId,
            String locationId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Find by Location
    Page<DailyConsumptionLog> findByLocationId(String locationId, Pageable pageable);

    // Find by Product
    Page<DailyConsumptionLog> findByProductId(String productId, Pageable pageable);

    // Find by Date Range
    Page<DailyConsumptionLog> findByConsumptionDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find latest consumption for product and location
    @Query(value = "{ 'productId': ?0, 'locationId': ?1 }", sort = "{ 'consumptionDate': -1, '_id': -1 }")
    Optional<DailyConsumptionLog> findLatestConsumptionByProductAndLocation(String productId, String locationId);

    // Check if consumption exists for specific date
    boolean existsByProductIdAndLocationIdAndConsumptionDate(String productId, String locationId, LocalDateTime consumptionDate);

    // Delete old consumption logs
    void deleteByConsumptionDateBefore(LocalDateTime cutoffDate);

    @Query(value = "{ 'productId': ?0, 'locationId': ?1 }",
            sort = "{ 'consumptionDate': -1, '_id': -1 }")
    List<DailyConsumptionLog> findLatestConsumptionByProductAndLocationWithLimit(
            String productId,
            String locationId,
            Pageable pageable
    );

    @Query("{'productId': ?0, 'locationId': ?1, 'consumptionDate': {$gte: ?2, $lte: ?3}}")
    List<DailyConsumptionLog> findConsumptionForPeriod(String productId, String locationId,
                                                       LocalDateTime startDate, LocalDateTime endDate);

    @Query("{'productId': ?0, 'locationId': ?1, 'consumptionDate': {$gte: ?2}}")
    List<DailyConsumptionLog> findRecentConsumption(String productId, String locationId, LocalDateTime cutoff);
}