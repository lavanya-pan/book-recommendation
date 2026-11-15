
package com.sds.bookrecommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class BookDataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;

    public BookDataLoader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Avoid importing duplicate books every time the app starts.
        if (bookRepository.count() > 0) {
            System.out.println("Books already exist. Skipping CSV import.");
            return;
        }

        Path csvPath = Path.of("src/main/resources/data/books.csv");

        if (!Files.exists(csvPath)) {
            System.out.println("books.csv not found. Skipping import.");
            return;
        }

        List<Book> books = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {

            String headerLine = reader.readLine();

            if (headerLine == null) {
                System.out.println("books.csv is empty.");
                return;
            }

            List<String> headers = parseCsvLine(headerLine);

            int titleIndex = headers.indexOf("title");
            int authorIndex = headers.indexOf("authors");
            int yearIndex = headers.indexOf("original_publication_year");
            int ratingIndex = headers.indexOf("average_rating");
            int countIndex = headers.indexOf("ratings_count");
            int imageIndex = headers.indexOf("image_url");

            if (titleIndex < 0 || authorIndex < 0) {
                System.out.println("Required CSV columns are missing.");
                return;
            }

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> columns = parseCsvLine(line);

                if (columns.size() <= Math.max(titleIndex, authorIndex)) {
                    continue;
                }

                String title = columns.get(titleIndex).trim();
                String author = columns.get(authorIndex).trim();

                if (title.isBlank()) {
                    continue;
                }

                if (author.isBlank()) {
                    author = "Unknown Author";
                }

                Integer year = parseInteger(columns, yearIndex);
                double rating = parseDouble(columns, ratingIndex);
                int ratingCount = parseInteger(columns, countIndex) == null
                        ? 0 : parseInteger(columns, countIndex);

                String coverUrl = getColumn(columns, imageIndex);

                // The dataset does not have a dedicated genre column.
                Book book = new Book(
                        title,
                        author,
                        "General",
                        null,
                        year
                );

                book.setCoverUrl(coverUrl);
                book.setAvgRating(rating);
                book.setRatingCount(ratingCount);

                books.add(book);
            }
        }

        bookRepository.saveAll(books);

        System.out.println("CSV import completed. Books imported: " + books.size());
    }

    private static String getColumn(List<String> columns, int index) {
        if (index < 0 || index >= columns.size()) {
            return null;
        }
        return columns.get(index).trim();
    }

    private static Integer parseInteger(List<String> columns, int index) {
        try {
            String value = getColumn(columns, index);
            if (value == null || value.isBlank()) {
                return null;
            }
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double parseDouble(List<String> columns, int index) {
        try {
            String value = getColumn(columns, index);
            if (value == null || value.isBlank()) {
                return 0.0;
            }
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Handles commas inside quoted CSV values and escaped quotes.
    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (current == '"') {
                if (insideQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        values.add(value.toString());
        return values;
    }
}
