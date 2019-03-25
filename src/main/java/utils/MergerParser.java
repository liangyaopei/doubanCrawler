package utils;

import java.util.List;
import java.util.stream.Stream;

public interface MergerParser {
    List<Integer> parse(Stream<String> lines);
}
