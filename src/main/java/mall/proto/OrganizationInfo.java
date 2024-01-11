package mall.proto;

import java.util.List;

public class OrganizationInfo {
    private long id;
    private String title;
    private String desc;
    private String icon;
    private List<School> schools;
    private List<Lesson> lessons;
    private long numOfCourse;
    private long numOfPerson;
    private long numOfSchool;
    public class Lesson {
        String name;
        int signNum;
        long id;
        String buyLessonDesc;
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSignNum() {
            return signNum;
        }

        public void setSignNum(int signNum) {
            this.signNum = signNum;
        }

        public String getBuyLessonDesc() {
            return buyLessonDesc;
        }

        public void setBuyLessonDesc(String buyLessonDesc) {
            this.buyLessonDesc = buyLessonDesc;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
    public class School{
        String name;
        double longitude;
        double latitude;
        long id;
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<School> getSchools() {
        return schools;
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public long getNumOfCourse() {
        return numOfCourse;
    }

    public void setNumOfCourse(long numOfCourse) {
        this.numOfCourse = numOfCourse;
    }

    public long getNumOfPerson() {
        return numOfPerson;
    }

    public void setNumOfPerson(long numOfPerson) {
        this.numOfPerson = numOfPerson;
    }

    public long getNumOfSchool() {
        return numOfSchool;
    }

    public void setNumOfSchool(long numOfSchool) {
        this.numOfSchool = numOfSchool;
    }
}
