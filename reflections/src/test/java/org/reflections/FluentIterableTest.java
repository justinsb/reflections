package org.reflections;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.reflections.filters.Filter;
import org.reflections.util.FluentIterable;
import org.reflections.util.Transformer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class FluentIterableTest {

    @Test
    public void iterateFilterForkFilterTransformFilter() {

        Iterable<Integer> integers = FluentIterable
                .iterate(Arrays.asList("a1", "b2", "c", "d4", "efghi"))
                .filter(len2)
                .fork(toChar)
                .filter(digits)
                .transform(integer)
                .filter(even);

        List<Integer> result = Arrays.asList(2, 4);

        List<Integer> integersList = Lists.newArrayList(integers);
        Assert.assertTrue(
                integersList.containsAll(result) && result.containsAll(integersList)
        );
    }

    private final Filter<String> len2 = new Filter<String>() {
        public boolean accept(final String s) {return s.length() == 2;}
    };

    private final Transformer<String, Iterator<Character>> toChar = new Transformer<String, Iterator<Character>>() {
        public Iterator<Character> transform(final String s) {
            return new AbstractIterator<Character>() {
                private int i;

                @Override
				protected Character computeNext() {
                    if (i < s.length()) {
                        return s.charAt(i++);
                    } else {
                        return endOfData();
                    }
                }
            };
        }
    };

    private final Filter<Character> digits = new Filter<Character>() {
        public boolean accept(final Character character) {
            return character >= '0' && character <= '9';
        }
    };

    private final Transformer<Character, Integer> integer = new Transformer<Character, Integer>() {
        public Integer transform(final Character character) {
            return Integer.valueOf(character) - 48;
        }
    };

    private final Filter<Integer> even = new Filter<Integer>() {
        public boolean accept(final Integer integer) {
            return integer % 2 == 0;
        }
    };
}
