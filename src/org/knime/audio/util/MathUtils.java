/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   May 13, 2016 (budiyanto): created
 */
package org.knime.audio.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
public class MathUtils {

	/**
	 * @param values a 2-Dimensional array whose mean should be calculated
	 * @return the mean of the given values
	 */
	public static double[] mean(final double[][] values){
		if(values == null){
			throw new IllegalArgumentException("Values cannot be null");
		}

		if(values.length == 1){
			return values[0];
		}

		// find the max number of dimensions
		int max = -1;
		for (int i = 0; i < values.length; ++i) {
			if ((values[i] != null) && (values[i].length > max)) {
				max = values[i].length;
			}
		}

		double[] result;
		if (max <= 0) {
			result = new double[]{0.0};
		} else {
			// now calculate means over all the dimensions
			result = new double[max];
			for (int i = 0; i < max; ++i) {
				int count = 0;
				double sum = 0.0;
				for (int j = 0; j < values.length; ++j) {
					if ((values[j] != null) && (values[j].length > i)) {
						sum += values[j][i];
						count++;
					}
				}
				if (count == 0) {
					result[i] = 0.0;
				} else {
					result[i] = sum / (count);
				}
			}
		}
		return result;
	}

	/**
	 * @param values a 2-Dimensional array whose mean should be calculated
	 * @return the standard deviation of the given values
	 */
	public static double[] standardDeviation(final double[][] values){
		if(values == null){
			throw new IllegalArgumentException("Values cannot be null");
		}

		if(values.length <= 1){
			return new double[values[0].length];
		}

		// find the max number of dimensions
		int max = -1;
		for (int i = 0; i < values.length; ++i) {
			if ((values[i] != null) && (values[i].length > max)) {
				max = values[i].length;
			}
		}

		final double[] result = new double[max];
		for(int col = 0; col < max; col++){
			int count = 0;
			double avg = 0.0;
			for(int row = 0; row < values.length; row++){
				if((values[row] != null) && (values[row].length > col)){
					avg += values[row][col];
					count++;
				}
			}
			avg /= count;
			for(int row = 0; row < values.length; row++){
				if((values[row] != null) && (values[row].length > col)){
					result[col] += Math.pow(values[row][col] - avg, 2);
				}
			}
			result[col] = Math.sqrt(result[col] / (count - 1));

		}
		return result;
	}

	public static List<double[]> derivative(final List<double[]> data) {
		final List<double[]> result = new ArrayList<double[]>(data.size());
		result.addAll(data);
		return result;
	}

}
