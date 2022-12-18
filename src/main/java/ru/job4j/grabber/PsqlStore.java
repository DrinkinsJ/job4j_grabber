package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class PsqlStore implements Store {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            LOG.error("Something broken while creating constructor", e);
        }
    }

    public static void main(String[] args) throws IOException {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            System.out.println(properties);
            try (PsqlStore psqlStore = new PsqlStore(properties)) {
                Post post1 = new Post("four", "four", "four", LocalDateTime.now());
                Post post2 = new Post("five", "five", "five", LocalDateTime.now());
                psqlStore.save(post1);
                psqlStore.save(post2);
                System.out.println(psqlStore.getAll());
                System.out.println(psqlStore.findById(1));
                System.out.println(psqlStore.findById(2));
            } catch (Exception e) {
                LOG.error("Something broken in main", e);
            }
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement preparedStatement = cnn.prepareStatement(
                "INSERT INTO post(name, text, link, created) VALUES(?,?,?,?)"
                        + "ON CONFLICT (link) DO NOTHING;", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            preparedStatement.execute();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Something broken while save posts", e);
        }
    }

    @Override
    public List<Post> getAll(){
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement preparedStatement = cnn.prepareStatement(
                "Select * from post;")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(setPost(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Something broken while getAll posts", e);
        }
        return posts;
    }

    @Override
    public Post findById(int id){
        Post post = null;
        try (PreparedStatement preparedStatement = cnn.prepareStatement(
                "SELECT * FROM post where id = ?;")) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    post = setPost(resultSet);
                }
            }
        } catch (SQLException e) {
            LOG.error("Something broken while findById posts", e);
        }
        return post;
    }

    private Post setPost(ResultSet resultSet) throws SQLException {
        return new Post(resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }
}