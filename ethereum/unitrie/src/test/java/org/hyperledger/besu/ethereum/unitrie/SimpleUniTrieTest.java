/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.hyperledger.besu.ethereum.unitrie;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public class SimpleUniTrieTest extends AbstractUniTrieTest {

  @Override
  DataLoader loader() {
    return __ -> Optional.empty();
  }

  @Override
  UniTrie<Bytes, String> createTrie() {
    return new SimpleUniTrie<>(
        s -> Objects.isNull(s) ? null : Bytes.wrap(s.getBytes(StandardCharsets.UTF_8)),
        v -> new String(v.toArrayUnsafe(), StandardCharsets.UTF_8));
  }
}
