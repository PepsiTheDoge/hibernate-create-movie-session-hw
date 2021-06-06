package mate.academy.dao.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {
    @Override
    public MovieSession add(MovieSession movieSession) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(movieSession);
            transaction.commit();
            return movieSession;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't insert movie session " + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(MovieSession.class, id));
        } catch (Exception e) {
            throw new RuntimeException("Can't get movie session by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> availableMovieSessionsQuery =
                    session.createQuery("from MovieSession ms "
                    + "where ms.movie.id = :movieId and ms.showtime between "
                    + ":startOfTheDay and :endOfTheDay", MovieSession.class);
            availableMovieSessionsQuery.setParameter("movieId", movieId);
            availableMovieSessionsQuery.setParameter("startOfTheDay", date.atStartOfDay());
            availableMovieSessionsQuery.setParameter("endOfTheDay", date.atTime(LocalTime.MAX));
            return availableMovieSessionsQuery.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Can't find available movie sessions by movie id: " + movieId
                    + " on this date: " + date, e);
        }
    }
}