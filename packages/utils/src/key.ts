const ALPHABET =
  '-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz';
const RANDOMNESS: number[] = [];
let lastTimestamp = 0;

/**
 * Generates lexically sortable unique keys based on:
 *
 * https://firebase.googleblog.com/2015/02/the-2120-ways-to-ensure-unique_68.html
 *
 * You can also consider the generated kys to be like UUIDS except:
 * (1) strictly increment from the generating node's point of view
 * (2) loosely increment based on relative machine time when viewed across nodes.
 */
export function key() {
  // first time setup.. initialize the randomness...
  if (RANDOMNESS.length === 0) {
    for (let i = 0; i < 12; i++) {
      RANDOMNESS[i] = Math.floor(Math.random() * 64);
    }
  }

  // we build the resulting key backwards. The most significant bits at the front of the key.
  let result = '';
  const timestamp = new Date().getTime();

  // Lets encode the random part of the key. (72 bits of randomness)
  // 72/6 = 12 base64 characters.

  // increment randomness when we being called too keys too quickly.
  if (timestamp === lastTimestamp) {
    for (let i = 0; i < 12; i++) {
      RANDOMNESS[i]++;
      if (RANDOMNESS[i] === 64) {
        RANDOMNESS[i] = 0; // we need to carry to the next random byte.
      } else {
        break; // done incrementing.
      }
    }
  }

  for (let i = 0; i < 12; i++) {
    result = ALPHABET.charAt(RANDOMNESS[i]) + result;
  }

  // Base64 encodes 6 bits of data per character. We want to encode
  // 6 bytes of the timestamp (48 bits), 48/6 = 8 base64 chars for the ts part
  // the key.
  let remainingTimestamp = timestamp;
  for (let i = 0; i < 8; i++) {
    result = ALPHABET.charAt(remainingTimestamp % 64) + result;
    remainingTimestamp = Math.floor(remainingTimestamp / 64);
  }

  lastTimestamp = timestamp;
  return result;
}
