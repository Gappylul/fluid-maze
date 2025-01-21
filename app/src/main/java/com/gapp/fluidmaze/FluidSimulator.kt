package com.gapp.fluidmaze

class FluidSimulator(private val width: Int, private val height: Int) {
    private val fluid = Array(height) { DoubleArray(width) { 0.0 } }

    fun step() {
        val diffusionRate = 0.1
        val tempFluid = Array(height) { DoubleArray(width) }

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                tempFluid[y][x] = (
                        fluid[y][x] +
                                diffusionRate * (
                                fluid[y - 1][x] + fluid[y + 1][x] +
                                        fluid[y][x - 1] + fluid[y][x + 1] - 4 * fluid[y][x]
                                )
                        )
            }
        }

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                fluid[y][x] = tempFluid[y][x] * 0.99
            }
        }
    }

    fun addSource(x: Int, y: Int, amount: Double) {
        if (x in 0 until width && y in 0 until height) {
            fluid[y][x] += amount
        }
    }

    fun getFluidData(): Array<DoubleArray> = fluid
}
