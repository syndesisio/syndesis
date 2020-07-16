/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dv.lsp.parser.statement;

public class TokenIndex {
    private int lastIndex;

    private int currentIndex;

    public TokenIndex(int numTokens) {
        if( numTokens > 0 ) {
            lastIndex = numTokens-1;
        }
    }

    public int current() {
        return currentIndex;
    }

    public void increment() {
        currentIndex++;
        //System.out.println("TokenIndex = " + this.toString());
    }

    public void increment(int delta) {
        currentIndex = currentIndex + delta;
    }

    public void decrement() {
        currentIndex--;
    }

    public void decrement(int delta) {
        if( currentIndex <= delta) {
            currentIndex = currentIndex - delta;
        } else {
            currentIndex = 0;
        }
    }

    public int next() {
        return currentIndex + 1;
    }

    public int previous() {
        return currentIndex - 1;
    }

    public boolean isLast() {
        return current() == lastIndex;
    }

    public boolean hasNext() {
        return current() < lastIndex;
    }

    public void set(int newCurrentIndex) {
        currentIndex = newCurrentIndex;
    }

    @Override
    public String toString() {
        return Integer.toString(currentIndex);
    }
}
