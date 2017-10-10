package net.sourceforge.jaad.aac.gain;

interface GCConstants {

	int BANDS = 4;
	int MAX_CHANNELS = 5;
	int NPQFTAPS = 96;
	int NPEPARTS = 64;	//number of pre-echo inhibition parts
	int ID_GAIN = 16;
	int[] LN_GAIN = {
		-4, -3, -2, -1, 0, 1, 2, 3,
		4, 5, 6, 7, 8, 9, 10, 11
	};
}
