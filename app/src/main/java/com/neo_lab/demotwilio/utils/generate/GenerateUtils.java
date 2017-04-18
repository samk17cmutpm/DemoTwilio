package com.neo_lab.demotwilio.utils.generate;

import java.util.Random;

/**
 * Created by sam_nguyen on 18/04/2017.
 */

public class GenerateUtils {
    private static final String ALLOWED_CHARACTERS ="0123456789";

    public static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}
