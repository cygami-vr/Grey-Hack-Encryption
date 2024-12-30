# What is this?
This is an implemementation of the RSA encryption scheme intended for use in the video game Grey Hack for securing a given SSH service.
Do NOT use this as a reference for implementing RSA in any real-world application. You should instead use established, reputable implementations by experts in the field.
Please note that I am not a security or cryptography expert, I just have a lot of experience with programming.

# How secure is this?
Not very. I believe 256 bit RSA can be cracked without too much trouble by those with experience.
However, it still offers security far higher than that of the out-of-box Caesar cipher available with the SSH service.
If real-life comparable security is important to you in Grey Hack, this code _will_ work with 1024 bit keys, but the encrypt/decrypt process will take ~2 minutes.

# How to use this
1. Download GenerateRSAKeypairForGreyHack.jar and run it from the command line like: `java -jar GenerateRSAKeypairForGreyHack.jar 512` where 512 is the number of bits you'd like. You can swap for 256 if you want quicker logins or 1024 if you want realistic (maybe) security.
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

# Optional optimization via password hardcoding
You can optionally hardcode a check for the correct encoded password in the `decode.bin`, allowing full bypass of decryption. This makes the login much faster, since the decryption takes much longer than the encryption.
With this method, use of 1024 bit keys is manageable with a log-in time of around 36 seconds.
This optimization is possible for two reasons.
1. Since Grey Hack _only_ uses encryption for the password, we can know what the single correct encoded value should look like.
2. The hardcoded checks are only needed in the `decode.bin`, which is not open source like `encode.src`.

It's possible this could change in the future as the game is developed. If you want to use this method, here are the steps to set it up:
1. Obtain the correct encoded string by running the code `print(RsaEncode("your password here", publicKey))`
2. Add the following logic to your `decode.bin`: `if encoded == "0xfakeEncodedPasswordString" then return "your password here"`
3. Leave the original return statement in place as a fallback. This way, in case the developers make a change to the game that causes this method to be too slow (for example, by using it for more than just the password), your server at least won't be _completely_ bricked, and you can switch to a lower bit-size keypair.
4. If your server has multiple possible users that may log in with different passwords (which is not advisable), you can add more hardcoded checks for the other passwords.
