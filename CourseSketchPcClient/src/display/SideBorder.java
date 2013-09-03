/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2011 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University
 *        nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written
 *        permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *
 *******************************************************************************/
package display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

public class SideBorder implements Border {
	int m_top;
	int m_left;
	int m_bottom;
	int m_right;

	boolean top = true;
	boolean right = true;
	boolean bottom = true;
	boolean left = true;

	Color m_color = Color.LIGHT_GRAY;

	public SideBorder() {
		m_top = 1;
		m_left = 1;
		m_bottom = 1;
		m_right = 1;
	}

	/**
	 *
	 * @param t top
	 * @param r right
	 * @param b bottom
	 * @param l left
	 */
	public SideBorder(boolean l, boolean t, boolean r, boolean b) {
		this();
		top = t;
		right = r;
		bottom = b;
		left = l;
	}

	public SideBorder(boolean l, boolean t, boolean r, boolean b, Color c) {
		this(l, t, r, b);
		m_color = c;
	}

	public void setColor(Color c) {
		m_color = c;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Insets insets = getBorderInsets(c);
		g.setColor(m_color);

		if (top)
			g.fillRect(0, 0, width - insets.right + 1, insets.top);

		if (right)
			g.fillRect(width - insets.right, 0, insets.right, height
					- insets.bottom + 1);

		if (bottom)
			g.fillRect(insets.left - 1, height - insets.bottom, width
					- insets.left + 1, insets.bottom);

		if (left)
			g.fillRect(0, 0, insets.right, height - insets.bottom + 1);
	}

	public Insets getBorderInsets(Component c) {
		return new Insets(m_top, m_left, m_bottom, m_right);
	}

	public boolean isBorderOpaque() {
		return true;
	}
}

