// $Id: JH256.java 255 2011-06-07 19:50:20Z tp $

package net.jacksum.zzadopt.fr.cryptohash;

/**
 * <p>This class implements the JH-256 digest algorithm under the
 * {@link Digest} API.</p>
 *
 * <pre>
 * ==========================(LICENSE BEGIN)============================
 *
 * Copyright (c) 2007-2010  Projet RNRT SAPHIR
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * ===========================(LICENSE END)=============================
 * </pre>
 *
 * @version   $Revision: 255 $
 * @author    Thomas Pornin &lt;thomas.pornin@cryptolog.com&gt;
 */

public class JH256 extends JHCore {

	private static final long[] IV = {
		0xeb98a3412c20d3ebL, 0x92cdbe7b9cb245c1L,
		0x1c93519160d4c7faL, 0x260082d67e508a03L,
		0xa4239e267726b945L, 0xe0fb1a48d41a9477L,
		0xcdb5ab26026b177aL, 0x56f024420fff2fa8L,
		0x71a396897f2e4d75L, 0x1d144908f77de262L,
		0x277695f776248f94L, 0x87d5b6574780296cL,
		0x5c5e272dac8e0d6cL, 0x518450c657057a0fL,
		0x7be4d367702412eaL, 0x89e3ab13d31cd769L
	};

	/**
	 * Create the engine.
	 */
	public JH256()
	{
	}

	/** @see Digest */
	public Digest copy()
	{
		return copyState(new JH256());
	}

	/** @see Digest */
	public int getDigestLength()
	{
		return 32;
	}

	/** @see JHCore */
	long[] getIV()
	{
		return IV;
	}
}
