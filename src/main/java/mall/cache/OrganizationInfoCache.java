package mall.cache;

import mall.models.*;
import mall.proto.OrganizationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Component
public class OrganizationInfoCache {
    public final static Map<Long, OrganizationInfo> OrgCacheEntity = new HashMap<>();
    @Autowired
    private final OrganizationRepository orgRepository;

    @Autowired
    private final OrganizationLessonRepository lessonRepository;

    @Autowired
    private final OrganizationSchoolRepository schoolRepository;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OrganizationInfoCache(OrganizationRepository orgRepository, OrganizationSchoolRepository
                                  schoolRepository, OrganizationLessonRepository lessonRepository) {
        this.orgRepository = orgRepository;
        this.lessonRepository = lessonRepository;
        this.schoolRepository = schoolRepository;
        List<Organization> orgList = this.orgRepository.findAll();
        List<OrganizationLesson> lessonList =  this.lessonRepository.findAll();
        List<OrganizationSchool> schoolList = this.schoolRepository.findAll();
        for (Organization org : orgList) {
            OrganizationInfo orgInfo = new OrganizationInfo();
            orgInfo.setId(org.getOrgId());
            orgInfo.setDesc(org.getIntro());
            orgInfo.setIcon(org.getIconUrl());
            orgInfo.setTitle(org.getTitle());
            List<OrganizationInfo.Lesson> lessons = new ArrayList<>();
            List<OrganizationInfo.School> schools = new ArrayList<>();
            orgInfo.setLessons(lessons);
            orgInfo.setSchools(schools);
            orgInfo.setNumOfPerson(0);
            orgInfo.setNumOfSchool(0);
            orgInfo.setNumOfCourse(0);
            OrgCacheEntity.put(org.getOrgId(), orgInfo);
        }
        for (OrganizationLesson lesson: lessonList) {
            OrganizationInfo.Lesson lessonInfo = OrgCacheEntity.get(lesson.getOrgId()).new Lesson();
            lessonInfo.setName(lesson.getName());
            lessonInfo.setBuyLessonDesc(lesson.getBuyLessonDesc());
            lessonInfo.setSignNum(lesson.getSignNums());
            lessonInfo.setId(lesson.getLessonId());
            Long signTotal = OrgCacheEntity.get(lesson.getOrgId()).getNumOfPerson() + lesson.getSignNums();
            OrgCacheEntity.get(lesson.getOrgId()).getLessons().add(lessonInfo);
            OrgCacheEntity.get(lesson.getOrgId()).setNumOfPerson(signTotal);
        }
        for (OrganizationSchool school: schoolList) {
            OrganizationInfo.School schoolInfo = OrgCacheEntity.get(school.getOrgId()).new School();
            schoolInfo.setName(school.getName());
            schoolInfo.setLatitude(school.getLatitude());
            schoolInfo.setLongitude(school.getLongitude());
            schoolInfo.setId(school.getSchoolId());
            OrgCacheEntity.get(school.getOrgId()).getSchools().add(schoolInfo);
        }
        for (Organization org : orgList) {
            long numOfCourse = OrgCacheEntity.get(org.getOrgId()).getLessons().size();
            OrgCacheEntity.get(org.getOrgId()).setNumOfCourse(numOfCourse);
            long numOfSchool = OrgCacheEntity.get(org.getOrgId()).getSchools().size();
            OrgCacheEntity.get(org.getOrgId()).setNumOfSchool(numOfSchool);
        }
        try {
            Iterator<Map.Entry<Long, OrganizationInfo>> iterator = OrgCacheEntity.entrySet().iterator();
            while (iterator.hasNext()) {
                logger.info("=================A new Org record=======================");
                Map.Entry<Long, OrganizationInfo> entry = iterator.next();
                logger.info("{} {} {} {} {}", entry.getKey(), entry.getValue().getDesc(),
                        entry.getValue().getIcon(), entry.getValue().getTitle(),
                        entry.getValue().getId());
                for (OrganizationInfo.Lesson lesson : entry.getValue().getLessons()) {
                    logger.info("{} {} {} {}", lesson.getId(),lesson.getName() ,lesson.getSignNum(),lesson.getBuyLessonDesc());
                }
                for (OrganizationInfo.School school : entry.getValue().getSchools()) {
                    logger.info("{} {} {} {}", school.getId(), school.getName() ,school.getLatitude(), school.getLongitude());
                }
            }
        } catch (Exception err) {

        }

    }


}
