package com.cinema.entity;

import javax.persistence.*;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 100)
    private String director;

    @Column(name = "film_cast", length = 500)
    private String cast;

    @Column(length = 50)
    private String genre;

    private Integer duration;

    @Column(length = 20)
    private String language;

    @Column(length = 20)
    private String releaseDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String posterBg;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "trailer_url", length = 200)
    private String trailerUrl;

    @Column(length = 50)
    private String country;

    @Column(name = "is_hot")
    private Boolean isHot = false;

    @Column(name = "box_office")
    private Double boxOffice = 0.0;

    private Double rating;

    @Column(length = 20)
    private String status = "showing";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterBg() { return posterBg; }
    public void setPosterBg(String posterBg) { this.posterBg = posterBg; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Boolean getIsHot() { return isHot; }
    public void setIsHot(Boolean isHot) { this.isHot = isHot; }
    public Double getBoxOffice() { return boxOffice; }
    public void setBoxOffice(Double boxOffice) { this.boxOffice = boxOffice; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
