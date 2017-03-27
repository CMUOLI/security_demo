package edu.cmu.oli.secure.domain;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author Raphael Gachuhi
 */
@Entity
@Table(name = "course_section", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"admit_code"})})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "CourseSection.findAll", query = "SELECT a FROM CourseSection a"),
        @NamedQuery(name = "CourseSection.findByGuid", query = "SELECT a FROM CourseSection a WHERE a.guid = :guid"),
        @NamedQuery(name = "CourseSection.findByTitle", query = "SELECT a FROM CourseSection a WHERE a.id = :id"),
        @NamedQuery(name = "CourseSection.findByCreated", query = "SELECT a FROM CourseSection a WHERE a.dateCreated = :dateCreated")})
public class CourseSection implements Serializable {

    private static final long serialVersionUID = 1L;
    @Expose()
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(max = 32)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "guid")
    private String guid;

    @Expose()
    @NotNull
    @Size(max = 50)
    @Column(name = "admit_code")
    private String admitCode;

    @Expose()
    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Expose()
    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date dateUpdated;

    @XmlTransient
    @OneToMany(mappedBy = "courseSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<LearningActivity> learningActivities;

    @XmlTransient
    @OneToMany(mappedBy = "courseSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Registration> registrations;

    public CourseSection() {
        this.dateCreated = new Date();
        this.dateUpdated = (Date) dateCreated.clone();
    }

}
