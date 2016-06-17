/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.java.model;

import java.lang.annotation.Annotation;

enum Color {
    RED, GREEN, BLUE
}

@interface MyAnno {
    byte byteValue();
    short shortValue();
    int intValue();
    char charValue();
    boolean booleanValue();
    boolean booleanTrueValue();
    long longValue();
    float floatValue();
    double doubleValue();
    
    String stringValue();
    String nullStringValue();
    
    byte[] byteArrayValue();
    short[] shortArrayValue();
    int[] intArrayValue();
    char[] charArrayValue();
    boolean[] booleanArrayValue();
    long[] longArrayValue();
    float[] floatArrayValue();
    double[] doubleArrayValue();
    
    String[] stringArrayType();
    Color[] enumValues();
    
    Color enumValue();
    
    Class<?> implClassValue();
}

class MyAnnoImpl implements MyAnno {
    @Override
    public byte byteValue() {
        return -128;
    }

    @Override
    public short shortValue() {
        return 1000;
    }

    @Override
    public int intValue() {
        return 1000;
    }

    @Override
    public char charValue() {
        return '~';
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public boolean booleanTrueValue() {
        return true;
    }

    @Override
    public long longValue() {
        return 1000L;
    }

    @Override
    public float floatValue() {
        return 5.0f;
    }

    @Override
    public double doubleValue() {
        return 6.0d;
    }

    @Override
    public String stringValue() {
        return "ABC";
    }

    @Override
    public String nullStringValue() {
        return null;
    }
    
    @Override
    public byte[] byteArrayValue() {
        return new byte[] { -100, -110, -120 };
    }

    @Override
    public short[] shortArrayValue() {
        return new short[] { 1000, 2000, 3000 };
    }

    @Override
    public int[] intArrayValue() {
        return new int[] { 1000, 2000, 3000 };
    }

    @Override
    public char[] charArrayValue() {
        return new char[] { 'A', 'B', 'C' };
    }

    @Override
    public boolean[] booleanArrayValue() {
        return new boolean[] { true, false, false };
    }

    @Override
    public long[] longArrayValue() {
        return new long[] { 500, 600, 700 };
    }

    @Override
    public float[] floatArrayValue() {
        return new float[] { 500, 600, 700 };
    }

    @Override
    public double[] doubleArrayValue() {
        return new double[] { 500, 600, 700 };
    }

    @Override
    public Color enumValue() {
        return Color.RED;
    }

    @Override
    public Class<?> implClassValue() {
        return Color.class;
    }

    @Override
    public String[] stringArrayType() {
        return new String[] { "ABC", "CDE", "FDSD" };
    }

    @Override
    public Color[] enumValues() {
        return new Color[] { Color.BLUE, Color.RED };
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return MyAnno.class;
    }
}