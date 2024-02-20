# What is this?
This is an implemementation of the RSA encryption scheme intended for use in the video game Grey Hack for securing a given SSH service.
Do NOT use this as a reference for implementing RSA in any real-world application. You should instead use established, reputable implementations by experts in the field.
Please note that I am not a security or cryptography expert, I just have a lot of experience with programming.

# How secure is this?
Not very. I believe 256 bit RSA can be cracked without too much trouble by those with experience.
However, it still offers security far higher than that of the out-of-box Caesar cipher available with the SSH service.
If real-life comparable security is important to you in Grey Hack, this code _will_ work with 1024 bit keys, but the encrypt/decrypt process will take ~2 minutes.

# How to use this
1. Generate a keypair
2. In Grey Hack, open CodeEditor.exe.
3. Paste in both BigInteger.src and RsaCrypto.src
4. Add the following at the bottom with your public key populated:

        publicKey = PublicKey.newKey(...) // TODO populate your public key here

        Encode = function(cleartext)
            return RsaEncode(cleartext, publicKey)
        end function

6. **Save** the file as `/server/encode.src`
7. Remove the lines added during step 4.
8. Add the following at the bottom with your private key populated:

        privateKey = PrivateKey.newKey(...) // TODO populate your private key here

        Decode = function(encoded)
            return RsaDecode(encoded, privateKey)
        end function

9. **Compile** the file as `/server/decode.bin`. Do not save the source code with your private key in it in-game, and instead save this information outside of the game.
10. Don't forget to enable encryption in `/server/conf/sshd.conf`
11. Test that it is working properly **in a separate terminal** before disconnecting your current session. If you disconnect, and there is a problem, you may be unable to access your server via SSH.

## How to generate your keypair

For optimization, this code uses a few precomputed constants. This project does **not** include a key generator.
To start, I will assume you have the typical RSA keypair constants already generated.
This includes:
1. Prime numbers `p` and `q`
2. Public key exponent `e`
3. Private key exponent `d`

This guide will **not** cover how to generate a secure `p` and `q` values.
In addition to these, you will need to compute the following:
1. `n = p * q`
2. `bitLen = ceil(log2(n))`
3. `blockSize = floor(bitLen / 8)`
4. `rrm = (1 << (bitLen * 2)) % n`
5. `qModInvP = q^(-1) (mod p)`
6. `dp = d % (p - 1)`
7. `dq = d % (q - 1)`
8. `pShift = ceil(log2(p)) * 2`
9. `pFactor = (1 << pShift) / p`
10. `qShift = ceil(log2(q)) * 2`
11. `qFactor = (1 << qShift) / q`

Using these, construct your keypairs as follows. Each number should be in a string hexadecimal representation such as `"0xabc123"`.
                
        publicKey = PublicKey.newKey(e, n, bitLen, rrm, blockSize)
        privateKey = PrivateKey.newKey(d, n, p, q, qModInvP, dp, dq, pShift, pFactor, qShift, qFactor)

# Roadmap
1. Consider implementing ECDSA encryption.
2. Optimize further to make 1024 bit keys practical. Currently I am unsure of further ways to optimize the code.
   - Probably the biggest opportunity is to reduce array allocations and calls to `list.add` and `list.remove`.
