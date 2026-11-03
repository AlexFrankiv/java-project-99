package hexlet.code.app.repository;

import hexlet.code.app.model.Task;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {
    public static Specification<Task> titleContains(String titleCont) {
        return (root, query, cb) -> titleCont == null ? null :
                cb.like(cb.lower(root.get("name")), "%" + titleCont.toLowerCase() + "%");
    }

    public static Specification<Task> hasAssigneeId(Long assigneeId) {
        return (root, query, cb) -> assigneeId == null ? null :
                cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null :
                cb.equal(root.get("taskStatus").get("slug"), status);
    }

    public static Specification<Task> hasLabelId(Long labelId) {
        return (root, query, cb) -> {
            if (labelId == null) {
                return null;
            }
            query.distinct(true);
            return cb.equal(root.join("labels").get("id"), labelId);
        };
    }
}
