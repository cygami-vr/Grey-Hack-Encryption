package com.github.cygamivr.greyhack.rsa;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class GenerateKeypair {
	
	// This is not intended for real life security purposes, but let's use the most cryptographically secure PRNG anyway, just for fun.
	private static Random rnd = new SecureRandom();

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Missing argument. Please specify the desired key length in bits.");
			return;
		}
		
		var bitLen = Integer.parseInt(args[0]) / 2;
		var keypair = Keypair.generatePrimeNumbers(bitLen);
		var e = new BigInteger("65537");
		
		// each char is 1 byte (ff)
		var blockSize = keypair.n.bitLength() / 8;	
		
		var nMR = MontgomeryReduction.constantsFor(keypair.n);
		
		String publicKey = String.format("publicKey = PublicKey.newKey(\"0x%s\", \"0x%s\", %s, \"%s\", %s)",
				e.toString(16), keypair.n.toString(16), nMR.bitLength, nMR.rrm, blockSize);
		
		BigInteger phi = keypair.p.subtract(BigInteger.ONE).multiply(keypair.q.subtract(BigInteger.ONE));
		BigInteger d = e.modInverse(phi);
		
		var crt = ChineseRemainderTheorem.constantsFor(keypair.p, keypair.q, d);
		
		String privateKey = String.format("privateKey = PrivateKey.newKey(\"0x%s\", \"0x%s\", \"0x%s\", \"0x%s\", \"%s\", \"%s\", \"%s\", %s, \"%s\", %s, \"%s\")",
				d.toString(16), keypair.n.toString(16), keypair.p.toString(16), keypair.q.toString(16), crt.qModInvP, crt.dp, crt.dq, crt.brcP.shift, crt.brcP.factor, crt.brcQ.shift, crt.brcQ.factor);
		
		System.out.println("\n~~~~~~~~ Keypair generation complete. Copy-paste the lines below as appropriate; Follow the remaining instructions on the README at https://github.com/cygami-vr/Grey-Hack-Encryption. ~~~~~~~~\n");
		
		System.out.println(publicKey);
		System.out.println();
		System.out.println(privateKey);
	}
	
	private static String toHex(BigInteger bi) {
		return "0x" + bi.toString(16);
	}
	
	private static class Keypair {
		
		private static Keypair generatePrimeNumbers(int bitLen) {
			var keypair = new Keypair();
			System.out.print("Generating prime numbers P and Q...");
			
			// Fermat factorization test
			int attempts = 0;
			boolean pass;
			do {

				attempts++;
				// Test essentially always fails when generating two values of the same bit length. Use # of attempts as an offset to bit length.
				keypair.p = BigInteger.probablePrime(bitLen + attempts, rnd);
				keypair.q = BigInteger.probablePrime(bitLen - attempts, rnd);
				keypair.n = keypair.p.multiply(keypair.q);
				var diff = keypair.p.subtract(keypair.q).abs();
				var threshold = keypair.n.shiftRight(1).sqrt();
				pass = diff.compareTo(threshold) > 0;
				
			} while (!pass);

			System.out.println(" done. Took " + attempts + " attempt(s).");
			System.out.println("Got P = " + keypair.p);
			System.out.println("Got Q = " + keypair.q);
			System.out.println("The probability that each number is not a prime is at most roughly 1 in 1 nonillion. That's 1 in 1 million trillion trillion. But if you want you can still ask wolframalpha or something to confirm that it's a prime number.");
			return keypair;
		}
		
		private BigInteger p, q, n;
	}
	
	private static class BarrettReduction {
		
		public static BarrettReduction constantsFor(BigInteger bi) {
			
			int shift = bi.bitLength() * 2;
			var factor = BigInteger.ONE.shiftLeft(shift).divide(bi);
			
			return new BarrettReduction(Integer.toString(shift), toHex(factor));
		}
		
		public final String shift, factor;

		public BarrettReduction(String shift, String factor) {
			this.shift = shift;
			this.factor = factor;
		}
	}

	private static class MontgomeryReduction {
	
		public static MontgomeryReduction constantsFor(BigInteger bi) {
			
			int bitLen = bi.bitLength();
			var rrm = BigInteger.ONE.shiftLeft(bitLen * 2).mod(bi);
			
			return new MontgomeryReduction(Integer.toString(bitLen), toHex(rrm));
		}
	
		public final String bitLength, rrm;
	
		public MontgomeryReduction(String bitLength, String rrm) {
			this.bitLength = bitLength;
			this.rrm = rrm;
		}
	}
	
	private static class ChineseRemainderTheorem {

		public static ChineseRemainderTheorem constantsFor(BigInteger p, BigInteger q, BigInteger d) {
			
			var dp = d.modPow(BigInteger.ONE, p.subtract(BigInteger.ONE));
			var dq = d.modPow(BigInteger.ONE, q.subtract(BigInteger.ONE));
			var brcP = BarrettReduction.constantsFor(p);
			var brcQ = BarrettReduction.constantsFor(q);
			var qModInvP = q.modInverse(p);
			
			return new ChineseRemainderTheorem(toHex(dp), toHex(dq), toHex(qModInvP), brcP, brcQ);
		}

		public final String dp, dq, qModInvP;
		public final BarrettReduction brcP, brcQ;

		public ChineseRemainderTheorem(String dp, String dq, String qModInvP, BarrettReduction brcP, BarrettReduction brcQ) {
			this.dp = dp;
			this.dq = dq;
			this.qModInvP = qModInvP;
			this.brcP = brcP;
			this.brcQ = brcQ;
		}
	}
}
