package tw.firemaples.onscreenocr.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class JsonUtilTest {

    private JsonTest expected;
    private String jsonString;
    private String failedJsonString;
    private String jsonArrayString;

    @Before
    public void setUp() throws Exception {
        expected = new JsonTest("string", 123, 1234567890, true);
        jsonString = "{\"aStr\":\"string\",\"aInt\":123,\"aLong\":1234567890,\"aBool\":true}";
        failedJsonString = "{\"aStr\":\"strin\",\"aInt\":12,\"aLong\":123456789,\"aBool\":false}";
        jsonArrayString = "[{\"aStr\":\"string\",\"aInt\":123,\"aLong\":1234567890,\"aBool\":true},{\"aStr\":\"string\",\"aInt\":123,\"aLong\":1234567890,\"aBool\":true}]";
    }

    @Test
    public void parseObjectByClass() {
        JsonTest result = new JsonUtil<JsonTest>().parseJson(jsonString, JsonTest.class);

        assertEquals(expected, result);
    }

    @Test
    public void parseObjectByTypeReference() {
        JsonTest result = new JsonUtil<JsonTest>().parseJson(jsonString, new TypeReference<JsonTest>() {
        });

        assertEquals(expected, result);
    }

    @Test
    public void parseArrayByClass() {
        List<JsonTest> result = new JsonUtil<List<JsonTest>>().parseJson(jsonArrayString, new TypeReference<List<JsonTest>>() {
        });

        assertEquals(expected, result.get(0));
        assertEquals(expected, result.get(1));
    }

    @Test
    public void parseObjectByClass_FailedTest() {
        JsonUtil<JsonTest> util = new JsonUtil<>();

        JsonTest result = util.parseJson(failedJsonString, JsonTest.class);
        assertNotEquals(expected, result);

        result = null;
        try {
            result = util.parseJson("{}", JsonTest.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotEquals(expected, result);

        result = null;
        try {
            result = util.parseJson("[]", JsonTest.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotEquals(expected, result);

        result = null;
        try {
            result = util.parseJson("", JsonTest.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotEquals(expected, result);
    }

    @Test
    public void parseObjectByTypeReference_FailedTest() {
        JsonUtil<JsonTest> util = new JsonUtil<>();
        TypeReference<JsonTest> type = new TypeReference<JsonTest>() {
        };

        JsonTest result = util.parseJson(failedJsonString, type);
        assertNotEquals(expected, result);

        result = null;
        try {
            result = util.parseJson("{}", type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotEquals(expected, result);

        result = null;
        try {
            result = util.parseJson("[]", type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotEquals(expected, result);

        result = null;
        try {
            result = util.parseJson("", type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotEquals(expected, result);
    }

    private class JsonTest {
        private String aStr;
        private int aInt;
        private long aLong;
        private boolean aBool;

        JsonTest(String aStr, int aInt, long aLong, boolean aBool) {
            this.aStr = aStr;
            this.aInt = aInt;
            this.aLong = aLong;
            this.aBool = aBool;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof JsonTest) {
                JsonTest other = (JsonTest) obj;

                return Objects.equals(this.aStr, other.aStr) &&
                        Objects.equals(this.aInt, other.aInt) &&
                        Objects.equals(this.aLong, other.aLong) &&
                        Objects.equals(this.aBool, other.aBool);
            }
            return false;
        }
    }
}