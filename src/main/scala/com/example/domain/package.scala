package com.example

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval.Closed
package object domain {
  type PositiveIntUpto20 = Int Refined Closed[1, 20]
}
