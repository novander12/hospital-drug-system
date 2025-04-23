package com.example.hospital.service;

import com.example.hospital.model.Drug;
import com.example.hospital.model.OperationLog;
import com.example.hospital.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;
    
    /**
     * 根据条件筛选并分页获取所有操作日志 (管理员使用)
     *
     * @param filterUsername   筛选的操作用户名 (可选)
     * @param filterActionType 筛选的操作类型 (可选)
     * @param filterResult     筛选的操作结果 (可选, e.g., "SUCCESS", "FAILURE")
     * @param startDate        筛选的开始时间 (可选)
     * @param endDate          筛选的结束时间 (可选)
     * @param keyword          筛选详情或药品名称的关键字 (可选)
     * @param pageable         分页和排序信息
     * @return 分页后的操作日志
     */
    public Page<OperationLog> findLogsFiltered(
            String filterUsername, String filterActionType, String filterResult,
            LocalDateTime startDate, LocalDateTime endDate, String keyword,
            Pageable pageable) {

        Specification<OperationLog> spec = buildSpecification(filterUsername, filterActionType, filterResult, startDate, endDate, keyword);
        return operationLogRepository.findAll(spec, pageable);
    }

    /**
     * 根据条件筛选并分页获取当前用户的操作日志
     *
     * @param currentUsername 当前登录用户名 (从 SecurityContext 获取)
     * @param filterActionType 筛选的操作类型 (可选)
     * @param filterResult     筛选的操作结果 (可选)
     * @param startDate        筛选的开始时间 (可选)
     * @param endDate          筛选的结束时间 (可选)
     * @param keyword          筛选详情或药品名称的关键字 (可选)
     * @param pageable         分页和排序信息
     * @return 分页后的操作日志
     */
    public Page<OperationLog> findCurrentUserLogsFiltered(
            String currentUsername, String filterActionType, String filterResult,
            LocalDateTime startDate, LocalDateTime endDate, String keyword,
            Pageable pageable) {

        Specification<OperationLog> spec = buildSpecification(currentUsername, filterActionType, filterResult, startDate, endDate, keyword);
        return operationLogRepository.findAll(spec, pageable);
    }

    private Specification<OperationLog> buildSpecification(
            String username, String actionType, String result,
            LocalDateTime startDate, LocalDateTime endDate, String keyword) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(username)) {
                predicates.add(criteriaBuilder.equal(root.get("username"), username));
            }
            if (StringUtils.hasText(actionType)) {
                predicates.add(criteriaBuilder.equal(root.get("action"), actionType));
            }
            if (StringUtils.hasText(result)) {
                if (!"ALL".equalsIgnoreCase(result) && !result.isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("operationResult"), result));
                }
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate detailsLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("details").as(String.class)), likePattern);
                Predicate drugNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("drugName").as(String.class)), likePattern);
                predicates.add(criteriaBuilder.or(detailsLike, drugNameLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 记录一般操作
     * @param username 用户名
     * @param action 操作类型
     * @param result 操作结果
     * @param details 操作详情
     * @param drugId 药品ID
     * @param drugName 药品名称
     */
    public void logOperation(String username, String action, String result, String details, Long drugId, String drugName) {
        OperationLog log = new OperationLog(username, action, result, details, drugId, drugName);
        operationLogRepository.save(log);
    }

    /**
     * 记录一般操作
     * @param username 用户名
     * @param action 操作类型
     * @param details 操作详情
     */
    public void logOperation(String username, String action, String details) {
        logOperation(username, action, "SUCCESS", details, null, null);
    }
    
    /**
     * 记录添加药品操作 (Removed stock logging)
     */
    public void logAddDrug(Drug drug) {
        String username = getCurrentUsername();
        String details = "添加药品";
        logOperation(username, "ADD_DRUG", "SUCCESS", details, drug.getId(), drug.getName());
    }
    
    /**
     * 记录更新药品操作 (Removed stock logging)
     */
    public void logUpdateDrug(Drug oldDrug, Drug newDrug) {
        String username = getCurrentUsername();
        StringBuilder details = new StringBuilder("更新药品信息：");
        boolean changed = false;

        if (oldDrug == null || newDrug == null) return;

        if (oldDrug.getName() != null && !oldDrug.getName().equals(newDrug.getName())) {
            details.append("名称由 '").append(oldDrug.getName()).append("' 改为 '").append(newDrug.getName()).append("'; ");
            changed = true;
        }
        if (oldDrug.getSpec() != null && !oldDrug.getSpec().equals(newDrug.getSpec())) {
            details.append("规格由 '").append(oldDrug.getSpec()).append("' 改为 '").append(newDrug.getSpec()).append("'; ");
            changed = true;
        }
         if (oldDrug.getCategory() != null && !oldDrug.getCategory().equals(newDrug.getCategory())) {
            details.append("类别由 '").append(oldDrug.getCategory()).append("' 改为 '").append(newDrug.getCategory()).append("'; ");
            changed = true;
        }
         if (oldDrug.getSupplier() != null && !oldDrug.getSupplier().equals(newDrug.getSupplier())) {
            details.append("供应商由 '").append(oldDrug.getSupplier()).append("' 改为 '").append(newDrug.getSupplier()).append("'; ");
            changed = true;
        }

        if (changed) {
           logOperation(username, "UPDATE_DRUG", "SUCCESS", details.toString(), newDrug.getId(), newDrug.getName());
        }
    }
    
    /**
     * 记录删除药品操作 (Removed stock logging)
     */
    public void logDeleteDrug(Drug drug) {
        String username = getCurrentUsername();
        String details = "删除药品";
        logOperation(username, "DELETE_DRUG", "SUCCESS", details, drug.getId(), drug.getName());
    }
    
    /**
     * 记录批量删除药品操作
     */
    public void logBatchDeleteDrugs(List<Drug> drugs) {
        String username = getCurrentUsername();
        String details = String.format("批量删除%d种药品", drugs.size());
        logOperation(username, "BATCH_DELETE", "SUCCESS", details, null, "多个药品");
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "system";
        }
        return authentication.getName();
    }

    public void saveLog(OperationLog log) {
        operationLogRepository.save(log);
    }

    public List<String> getDistinctActionTypes() {
        List<String> dbTypes = operationLogRepository.findDistinctActionTypes();
        return dbTypes.stream().distinct().sorted().collect(Collectors.toList());
    }

    /**
     * 获取日志总数 (DataInitializer 使用)
     * @return 日志记录总数
     */
    public long countLogs() {
        return operationLogRepository.count();
    }
} 