package mall.models;

import javax.persistence.*;

@Entity
@Table(name = "organization_lesson")
public class OrganizationLesson {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lessonId;
    private Long orgId;

    private String name;

    private int signNums;

    private String buyLessonDesc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public int getSignNums() {
        return signNums;
    }

    public void setSignNums(int signNums) {
        this.signNums = signNums;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuyLessonDesc() {
        return buyLessonDesc;
    }

    public void setBuyLessonDesc(String buyLessonDesc) {
        this.buyLessonDesc = buyLessonDesc;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }
}
