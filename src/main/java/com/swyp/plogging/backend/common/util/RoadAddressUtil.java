package com.swyp.plogging.backend.common.util;

import com.swyp.plogging.backend.common.util.dto.Address;
import com.swyp.plogging.backend.common.util.dto.JibunAddress;
import com.swyp.plogging.backend.common.util.dto.RoadAddress;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RoadAddressUtil {
    private static final List<Pattern> patterns = new ArrayList<>();
    private static final Pattern gibunAddressPattern = Pattern.compile("(\\S+시)\\s+(\\S+구)\\s+(\\S+동)");
    private static Matcher matcher;

    @PostConstruct
    public void init() {
        // 도로명주소 구, 길, 번호 정규식 패턴
        Pattern guGilNum = Pattern.compile("(\\S+구)\\s+(\\S*(?:대로|로|길))\\s+(\\d*)");
        patterns.add(guGilNum);
        // 도로명주소 길, 번호 정규식 패턴
        Pattern gilNum = Pattern.compile("(\\S*(?:대로|로|길))\\s+(\\d*)");
        patterns.add(gilNum);
        // 도롤명주소 구 길 지하 번호 정규식 패턴
        Pattern guGilUnderNum = Pattern.compile("(\\S+구)\\s+(\\S*(?:대로|로|길))\\s+(지하)\\s+(\\d*)");
        patterns.add(guGilUnderNum);
        // 도로명주소 길 지하 번호 정규식 패턴
        Pattern gilUnderNum = Pattern.compile("(\\S*(?:대로|로|길))\\s+(지하)\\s+(\\d*)");
        patterns.add(gilUnderNum);
    }

    public static Address getAddressObject(String address) {
        if (!isRoadAddress(address)) {
            return new JibunAddress(matcher.group(1), matcher.group(2), matcher.group(3));
        }
        // 여기서부터 도로명주소 매쳐
        if (matcher.groupCount() < 3) {
            return new RoadAddress(null, null, matcher.group(1), false, Integer.parseInt(matcher.group(2)));
        }
        if (matcher.group(2).equals("지하")) {
            return new RoadAddress(null, null, matcher.group(1), true, Integer.parseInt(matcher.group(2)));
        }
        if (matcher.group(3).equals("지하")) {
            return new RoadAddress(null, matcher.group(1), matcher.group(2), true, Integer.parseInt(matcher.group(3)));
        }
        return new RoadAddress(null, matcher.group(1), matcher.group(2), false, Integer.parseInt(matcher.group(3)));
    }

    public static Address getAddressObject(CharSequence address) {
        return getAddressObject((String) address);
    }

    public static boolean isRoadAddress(String address) {
        matcher = gibunAddressPattern.matcher(address);
        if (!matcher.find()) {
            // 도로명주소다.
            for(Pattern pattern : patterns){
                matcher = pattern.matcher(address);
                if(matcher.find()){
                    return true;
                }
            }
        } else if (matcher.find()) {
            // 지번주소다.
            return false;
        }
        throw new RuntimeException("올바른 주소 형식이 아닙니다.");
    }

    public static boolean compareRoadAddress(Address a1, Address a2) {
        return a1.getFullName().equals(a2.getFullName());
    }
}
