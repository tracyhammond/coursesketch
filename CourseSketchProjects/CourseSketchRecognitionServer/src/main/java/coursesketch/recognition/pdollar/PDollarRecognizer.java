/**
 * The $P Point-Cloud Recognizer (JavaScript version)
 *
 * Radu-Daniel Vatavu, Ph.D.
 * University Stefan cel Mare of Suceava
 * Suceava 720229, Romania
 * vatavu@eed.usv.ro
 *
 * Lisa Anthony, Ph.D.
 * UMBC
 * Information Systems Department
 * 1000 Hilltop Circle
 * Baltimore, MD 21250
 * lanthony@umbc.edu
 *
 * Jacob O. Wobbrock, Ph.D.
 * The Information School
 * University of Washington
 * Seattle, WA 98195-2840
 * wobbrock@uw.edu
 *
 * The academic publication for the $P recognizer, and what should be
 * used to cite it, is:
 *
 * Vatavu, R.-D., Anthony, L. and Wobbrock, J.O. (2012).
 * Gestures as point clouds: A $P recognizer for user interface
 * prototypes. Proceedings of the ACM Int'l Conference on
 * Multimodal Interfaces (ICMI '12). Santa Monica, California
 * (October 22-26, 2012). New York: ACM Press, pp. 273-280.
 *
 * This software is distributed under the "New BSD License" agreement:
 *
 * Copyright (c) 2012, Radu-Daniel Vatavu, Lisa Anthony, and
 * Jacob O. Wobbrock. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the names of the University Stefan cel Mare of Suceava,
 * University of Washington, nor UMBC, nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Radu-Daniel Vatavu OR Lisa Anthony
 * OR Jacob O. Wobbrock BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 **/
package coursesketch.recognition.pdollar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//
// PDollarRecognizer class constants
//
public class PDollarRecognizer {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PDollarRecognizer.class);


    static int mNumPoints = 32;
    static Point mPointOrig = new Point(0.0, 0.0, 0);
    static List<PointCloud> mPntClouds = new ArrayList<PointCloud>();

    public PDollarRecognizer() {
        //initializePointCloudTable();
    }

    public RecognizerResults Recognize(List<Point> points) {

        PointCloud foundPointCloud = null;
        points = Resample(points, mNumPoints);
        points = Scale(points);
        points = TranslateTo(points, mPointOrig);

        double score = Double.POSITIVE_INFINITY;
        for (int i = 0; i < mPntClouds.size(); i++) // for each point-cloud template
        {
            double distScore = GreedyCloudMatch(points, mPntClouds.get(i));
            if (distScore < score) {
                score = distScore; // best (least) distance
                foundPointCloud = mPntClouds.get(i); // point-cloud
            }
        }
        return (foundPointCloud == null) ? new RecognizerResults("No match.", 0.0)
                : new RecognizerResults(foundPointCloud.mName, Math.max((score - 2.0) / -2.0, 0.0), String.format("score %f\n", score));
    }

    public int addGesture(String name, List<Point> points) {
        mPntClouds.add(new PointCloud(name, points, mNumPoints));
        int num = 0;
        for (int i = 0; i < mPntClouds.size(); i++) {
            if (mPntClouds.get(i).mName.equals(name))
                num++;
        }
        return num;
    }

    private static double GreedyCloudMatch(List<Point> points, PointCloud pntCloud) {
        double e = 0.50;
        double step = Math.floor(Math.pow(points.size(), 1 - e));

        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.size(); i += step) {
            double d1 = CloudDistance(points, pntCloud.mPoints, i);
            double d2 = CloudDistance(pntCloud.mPoints, points, i);
            min = Math.min(min, Math.min(d1, d2)); // min3
        }
        return min;
    }

    private static double CloudDistance(List<Point> pts1, List<Point> pts2, int start) {
        // pts1.size() == pts2.size()
        boolean[] matched = new boolean[pts1.size()];
        for (int k = 0; k < pts1.size(); k++)
            matched[k] = false;
        double sum = 0;
        int i = start;
        do {
            int index = -1;
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < matched.length; j++) {
                if (!matched[j]) {
                    double d = EuclideanDistance(pts1.get(i), pts2.get(j));
                    if (d < min) {
                        min = d;
                        index = j;
                    }
                }
            }
            matched[index] = true;
            double weight = 1 - ((i - start + pts1.size()) % pts1.size()) / pts1.size();
            sum += weight * min;
            i = (i + 1) % pts1.size();
        } while (i != start);
        return sum;
    }

    private static List<Point> Resample(List<Point> points, int n) {
        double I = PathLength(points) / (n - 1); // interval length
        double D = 0.0;

        List<Point> newpoints = new ArrayList<Point>();
        newpoints.add(points.get(0));

        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).ID.equals(points.get(i - 1).ID)) {
                double d = EuclideanDistance(points.get(i - 1), points.get(i));
                if ((D + d) >= I) {
                    double qx = points.get(i - 1).X + ((I - D) / d) * (points.get(i).X - points.get(i - 1).X);
                    double qy = points.get(i - 1).Y + ((I - D) / d) * (points.get(i).Y - points.get(i - 1).Y);
                    Point q = new Point(qx, qy, points.get(i).ID);
                    newpoints.add(q); // append new point 'q'
                    points.add(i, q); // insert 'q' at position i in points s.t. 'q' will be the next i
                    D = 0.0;
                } else {
                    D += d;
                }
            }
        }

        // sometimes we fall a rounding-error short of
        // adding the last point, so add it if so
        if (newpoints.size() == n - 1)
            newpoints.add(new Point(points.get(points.size() - 1).X, points.get(points.size() - 1).Y, points.get(points.size() - 1).ID));
        return newpoints;
    }

    private static List<Point> Scale(List<Point> points) {
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.size(); i++) {
            minX = Math.min(minX, points.get(i).X);
            minY = Math.min(minY, points.get(i).Y);
            maxX = Math.max(maxX, points.get(i).X);
            maxY = Math.max(maxY, points.get(i).Y);
        }

        double size = Math.max(maxX - minX, maxY - minY);
        List<Point> newpoints = new ArrayList<Point>();

        for (int i = 0; i < points.size(); i++) {
            double qx = (points.get(i).X - minX) / size;
            double qy = (points.get(i).Y - minY) / size;
            newpoints.add(new Point(qx, qy, points.get(i).ID));
        }
        return newpoints;
    }

    private static List<Point> TranslateTo(List<Point> points, Point pt) // translates points' centroid
    {
        Point c = Centroid(points);
        List<Point> newpoints = new ArrayList<Point>();
        for (int i = 0; i < points.size(); i++) {
            double qx = points.get(i).X + pt.X - c.X;
            double qy = points.get(i).Y + pt.Y - c.Y;
            newpoints.add(new Point(qx, qy, points.get(i).ID));
        }
        return newpoints;
    }

    private static Point Centroid(List<Point> points) {
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < points.size(); i++) {
            x += points.get(i).X;
            y += points.get(i).Y;
        }
        x /= points.size();
        y /= points.size();
        return new Point(x, y, 0);
    }

    // average distance between corresponding points in two paths
    private static double PathDistance(List<Point> pts1, List<Point> pts2) {
        double d = 0.0;
        for (int i = 0; i < pts1.size(); i++) // assumes pts1.size() == pts2.size()
            d += EuclideanDistance(pts1.get(i), pts2.get(i));
        return d / pts1.size();
    }

    // length traversed by a point path
    private static double PathLength(List<Point> points) {
        double d = 0.0;
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).ID.equals(points.get(i - 1).ID))
                d += EuclideanDistance(points.get(i - 1), points.get(i));
        }
        return d;
    }

    // Euclidean distance between two points
    private static double EuclideanDistance(Point p1, Point p2) {
        double dx = p2.X - p1.X;
        double dy = p2.Y - p1.Y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    //
    // PointCloud class: a point-cloud template
    //
    public class PointCloud {
        public List<Point> mPoints;
        public String mName;

        PointCloud(String name, List<Point> points, int numPoints) {

            mName = name;
            mPoints = points;

            mPoints = PDollarRecognizer.Resample(mPoints, numPoints);
            mPoints = PDollarRecognizer.Scale(mPoints);
            mPoints = PDollarRecognizer.TranslateTo(mPoints, mPointOrig);
        }
    }

    private void initializePointCloudTable() {
        mPntClouds.add(new PointCloud("T", new ArrayList<Point>(Arrays.asList(
                        new Point(30, 7, 1),
                        new Point(103, 7, 1),
                        new Point(66, 7, 2),
                        new Point(66, 87, 2))),
                        mNumPoints)
        );

        mPntClouds.add(new PointCloud("N", new ArrayList<Point>(Arrays.asList(
                        new Point(177, 92, 1), new Point(177, 2, 1),
                        new Point(182, 1, 2), new Point(246, 95, 2),
                        new Point(247, 87, 3), new Point(247, 1, 3)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("D", new ArrayList<Point>(Arrays.asList(
                        new Point(345, 9, 1), new Point(345, 87, 1),
                        new Point(351, 8, 2), new Point(363, 8, 2),
                        new Point(372, 9, 2), new Point(380, 11, 2),
                        new Point(386, 14, 2), new Point(391, 17, 2),
                        new Point(394, 22, 2), new Point(397, 28, 2),
                        new Point(399, 34, 2), new Point(400, 42, 2),
                        new Point(400, 50, 2), new Point(400, 56, 2),
                        new Point(399, 61, 2), new Point(397, 66, 2),
                        new Point(394, 70, 2), new Point(391, 74, 2),
                        new Point(386, 78, 2), new Point(382, 81, 2),
                        new Point(377, 83, 2), new Point(372, 85, 2),
                        new Point(367, 87, 2), new Point(360, 87, 2),
                        new Point(355, 88, 2), new Point(349, 87, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("P", new ArrayList<Point>(Arrays.asList(
                        new Point(507, 8, 1), new Point(507, 87, 1),
                        new Point(513, 7, 2), new Point(528, 7, 2), new Point(537, 8, 2), new Point(544, 10, 2),
                        new Point(550, 12, 2), new Point(555, 15, 2), new Point(558, 18, 2), new Point(560, 22, 2),
                        new Point(561, 27, 2), new Point(562, 33, 2), new Point(561, 37, 2), new Point(559, 42, 2),
                        new Point(556, 45, 2), new Point(550, 48, 2), new Point(544, 51, 2), new Point(538, 53, 2),
                        new Point(532, 54, 2), new Point(525, 55, 2), new Point(519, 55, 2), new Point(513, 55, 2),
                        new Point(510, 55, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("X", new ArrayList<Point>(Arrays.asList(
                        new Point(30, 146, 1), new Point(106, 222, 1),
                        new Point(30, 225, 2), new Point(106, 146, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("H", new ArrayList<Point>(Arrays.asList(
                        new Point(188, 137, 1), new Point(188, 225, 1),
                        new Point(188, 180, 2), new Point(241, 180, 2),
                        new Point(241, 137, 3), new Point(241, 225, 3)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("I", new ArrayList<Point>(Arrays.asList(
                        new Point(371, 149, 1), new Point(371, 221, 1),
                        new Point(341, 149, 2), new Point(401, 149, 2),
                        new Point(341, 221, 3), new Point(401, 221, 3)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("exclamation", new ArrayList<Point>(Arrays.asList(
                        new Point(526, 142, 1), new Point(526, 204, 1),
                        new Point(526, 221, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("line", new ArrayList<Point>(Arrays.asList(
                        new Point(12, 347, 1), new Point(119, 347, 1)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("Five Point Star", new ArrayList<Point>(Arrays.asList(
                        new Point(177, 396, 1), new Point(223, 299, 1), new Point(262, 396, 1),
                        new Point(168, 332, 1), new Point(278, 332, 1), new Point(184, 397, 1)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("NULL", new ArrayList<Point>(Arrays.asList(
                        new Point(382, 310, 1), new Point(377, 308, 1), new Point(373, 307, 1),
                        new Point(366, 307, 1), new Point(360, 310, 1), new Point(356, 313, 1),
                        new Point(353, 316, 1), new Point(349, 321, 1), new Point(347, 326, 1),
                        new Point(344, 331, 1), new Point(342, 337, 1), new Point(341, 343, 1),
                        new Point(341, 350, 1), new Point(341, 358, 1), new Point(342, 362, 1),
                        new Point(344, 366, 1), new Point(347, 370, 1), new Point(351, 374, 1),
                        new Point(356, 379, 1), new Point(361, 382, 1), new Point(368, 385, 1),
                        new Point(374, 387, 1), new Point(381, 387, 1), new Point(390, 387, 1),
                        new Point(397, 385, 1), new Point(404, 382, 1), new Point(408, 378, 1),
                        new Point(412, 373, 1), new Point(416, 367, 1), new Point(418, 361, 1),
                        new Point(419, 353, 1), new Point(418, 346, 1), new Point(417, 341, 1),
                        new Point(416, 336, 1), new Point(413, 331, 1), new Point(410, 326, 1),
                        new Point(404, 320, 1), new Point(400, 317, 1), new Point(393, 313, 1),
                        new Point(392, 312, 1), new Point(418, 309, 2), new Point(337, 390, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("arrowhead", new ArrayList<Point>(Arrays.asList(
                        new Point(506, 349, 1), new Point(574, 349, 1),
                        new Point(525, 306, 2), new Point(584, 349, 2),
                        new Point(525, 388, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("pitchfork", new ArrayList<Point>(Arrays.asList(
                        new Point(38, 470, 1), new Point(36, 476, 1), new Point(36, 482, 1), new Point(37, 489, 1),
                        new Point(39, 496, 1), new Point(42, 500, 1), new Point(46, 503, 1), new Point(50, 507, 1),
                        new Point(56, 509, 1), new Point(63, 509, 1), new Point(70, 508, 1), new Point(75, 506, 1),
                        new Point(79, 503, 1), new Point(82, 499, 1), new Point(85, 493, 1), new Point(87, 487, 1),
                        new Point(88, 480, 1), new Point(88, 474, 1), new Point(87, 468, 1),
                        new Point(62, 464, 2), new Point(62, 571, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("six-point star", new ArrayList<Point>(Arrays.asList(
                        new Point(177, 554, 1), new Point(223, 476, 1), new Point(268, 554, 1), new Point(183, 554, 1),
                        new Point(177, 490, 2), new Point(223, 568, 2), new Point(268, 490, 2), new Point(183, 490, 2)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("asterisk", new ArrayList<Point>(Arrays.asList(
                        new Point(325, 499, 1), new Point(417, 557, 1),
                        new Point(417, 499, 2), new Point(325, 557, 2),
                        new Point(371, 486, 3), new Point(371, 571, 3)
                )), mNumPoints)
        );

        mPntClouds.add(new PointCloud("half-note", new ArrayList<Point>(Arrays.asList(
                        new Point(546, 465, 1), new Point(546, 531, 1), new Point(540, 530, 2),
                        new Point(536, 529, 2), new Point(533, 528, 2), new Point(529, 529, 2),
                        new Point(524, 530, 2), new Point(520, 532, 2), new Point(515, 535, 2),
                        new Point(511, 539, 2), new Point(508, 545, 2), new Point(506, 548, 2),
                        new Point(506, 554, 2), new Point(509, 558, 2), new Point(512, 561, 2),
                        new Point(517, 564, 2), new Point(521, 564, 2), new Point(527, 563, 2),
                        new Point(531, 560, 2), new Point(535, 557, 2), new Point(538, 553, 2),
                        new Point(542, 548, 2), new Point(544, 544, 2), new Point(546, 540, 2),
                        new Point(546, 536, 2)
                )), mNumPoints)
        );
    }
}
