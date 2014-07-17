package console;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import tango.BasicBST;
import tango.BinarySearchTree;
import tango.RedBlackTango;
import tango.RedBlackTree;
import tango.SplayTree;
import tango.TreeStats;
import tango.UnbalancedTango;

public class StatRunner {
	private static final Logger log = Logger.getLogger(StatRunner.class);

	private static final String cTreeType = StatRunner.class.getSimpleName()
			+ ".treeType";
	private static final String cSets = StatRunner.class.getSimpleName()
			+ ".numSets";
	private static final String cMinLevels = StatRunner.class.getSimpleName()
			+ ".minLevels";
	private static final String cMaxLevels = StatRunner.class.getSimpleName()
			+ ".maxLevels";
	private static final String cSeqType = StatRunner.class.getSimpleName()
			+ ".sequenceType";
	private static final String cReps = StatRunner.class.getSimpleName()
			+ ".sequenceRepetitions";
	private static final String cSeqFile = StatRunner.class.getSimpleName()
			+ ".sequenceFile";

	/*
	 * Config
	 */
	private TreeType _treeType;
	private int _sets;
	private int _minLevels;
	private int _maxLevels;
	private int _reps;
	private String _seqFile;

	/*
	 * Current run
	 */
	private int _curLevels;
	private int _curN;
	private int _curSet;
	private SequenceType _curSeqType;
	private ArrayList<Integer> _sequence;
	private BinarySearchTree _tree;
	private Random _rnd = new Random();

	public void start() {
		_rnd = new Random();

		loadConfiguration();

		for (_curLevels = _minLevels; _curLevels <= _maxLevels; ++_curLevels) {
			for (_curSet = 0; _curSet < _sets; ++_curSet) {

				_curN = ((int) Math.pow(2, _curLevels)) - 1;

				genSequence();

				prepTree();

				execSequence();

				printResults();
			}
		}
	}

	private void loadConfiguration() {
		Config c = GlobalConfig.getInstance();

		/*
		 * Parse config
		 */
		try {

			_treeType = TreeType.fromString(c.getConfig(cTreeType));
			_sets = Integer.parseInt(c.getConfig(cSets));
			_minLevels = Integer.parseInt(c.getConfig(cMinLevels));
			_maxLevels = Integer.parseInt(c.getConfig(cMaxLevels));
			_reps = Integer.parseInt(c.getConfig(cReps));
			_curSeqType = SequenceType.fromString(c.getConfig(cSeqType));

			if (_curSeqType == SequenceType.FILE) {
				_seqFile = c.getConfig(cSeqFile);
			}

		} catch (NumberFormatException e) {
			log.fatal(e.getMessage());
			System.exit(-1);
		} catch (ItemNotFoundException e) {
			log.fatal(e.getMessage());
			System.exit(-1);
		}

		/*
		 * Check other conditions
		 */
		if (_treeType == TreeType.UNKNOWN) {
			log.fatal("Unknown Tree Type.");
			System.exit(-1);
		}

		if (_curSeqType == SequenceType.UNKNOWN) {
			log.fatal("Unknown Sequence Type.");
			System.exit(-1);
		}

		if (_minLevels < 1) {
			log.fatal("minLevels must be >= 1");
			System.exit(-1);
		}

		if (_maxLevels < _minLevels) {
			log.fatal("maxLevels must be at least equal to minLevels");
			System.exit(-1);
		}

	}

	private void genSequence() {
		_sequence = new ArrayList<Integer>();

		for (int i = 0; i < _reps; ++i) {
			switch (_curSeqType) {
			case FILE:
				loadSequence();
				break;
			case INCREASING:
				genIncreasingSequence(_curN);
				break;
			case DECREASING:
				genDecreasingSequence(_curN);
				break;
			case PERMUTATION:
				genPermutationSequence(_curN);
				break;
			case RANDOM:
				genRandomSequence(_curN);
				break;
			case SQUAREROOT:
				genSquareRootSequence(_curN);
				break;
			default:
				log.fatal("Unhandled sequence type");
				System.exit(-1);
				break;
			}
		}
	}

	private void genIncreasingSequence(int n) {
		log.info("Generating increasing sequence from 1 to " + n);

		for (int i = 1; i <= n; ++i) {
			_sequence.add(Integer.valueOf(i));
		}
	}

	private void genDecreasingSequence(int n) {
		log.info("Generating decreasing sequence from " + n + " to 1");

		for (int i = n; i > 0; --i) {
			_sequence.add(Integer.valueOf(i));
		}
	}

	private void genPermutationSequence(int n) {
		log.info("Generating a permutation of the values 1 to " + n);

		int[] arr = new int[n];

		// fill array
		for (int i = 0; i < n; ++i) {
			arr[i] = i + 1; // offset value
		}

		// permute!
		for (int i = 0; i < n - 1; ++i) {
			// select an element from anywhere in the remainder of the array
			int j = i + _rnd.nextInt(n - i);
			swap(arr, i, j);
		}

		// insert into sequence
		for (int i = 0; i < n; ++i) {
			_sequence.add(Integer.valueOf(arr[i]));
		}
	}

	private void genRandomSequence(int n) {
		log.info("Generating a random sequence of value from [1," + n + "]"
				+ " of length " + n);

		for (int i = 1; i <= n; ++i) {
			_sequence.add(_rnd.nextInt(n) + 1);
		}
	}

	private void genSquareRootSequence(int n) {
		log.info("Generating Sqrt sequence of the values 1 to " + n);

		int sqr = (int) (Math.floor(Math.sqrt(n)));

		for (int i = 1; i <= sqr; ++i) {
			int val = i;

			while (val <= n) {
				_sequence.add(Integer.valueOf(val));

				val += sqr;
			}
		}
	}

	private void loadSequence() {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(_seqFile));

		} catch (FileNotFoundException e) {
			log.fatal(_seqFile + " could not be found for opening");
			System.exit(-1);
		}

		String line = "";

		try {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				Integer i = Integer.valueOf(line);
				_sequence.add(i);
			}

			br.close();

		} catch (NumberFormatException e) {
			log.fatal("Error parsing '" + line + "' from file");
			System.exit(-1);
		} catch (IOException e) {
			log.fatal("IOException processing file");
			System.exit(-1);
		}

		log.info("Loaded " + _seqFile + " with " + _sequence.size() + " items");
	}

	private void prepTree() {

		if (_treeType == TreeType.RBTANGO) {
			_tree = new RedBlackTango();
		} else if (_treeType == TreeType.UTANGO) {
			_tree = new UnbalancedTango();
		} else if (_treeType == TreeType.SPLAY) {
			_tree = new SplayTree();
		} else if (_treeType == TreeType.REDBLACK) {
			_tree = new RedBlackTree();
		} else if (_treeType == TreeType.BASIC) {
			_tree = new BasicBST();
		} else {
			log.fatal("Unhandled tree type");
			System.exit(-1);
		}

		_tree.initializePerfectTree(_curLevels);
	}

	private void execSequence() {
		int n = _sequence.size();

		log.info("Executing search sequence of length " + n + "...");
		log.debug("Sequence is: " + _sequence);

		try {
			for (int i = 0; i < n; ++i) {
				_tree.search(_sequence.get(i));
			}
		} catch (AssertionError e) {
			log.fatal("Assertion Failure: " + e.getMessage());

			// but, we move on without exiting

		} catch (Exception e) {
			log.fatal("Unexpected Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void printResults() {
		String fmt = "Results: treetype:%s;seqtype:%s;levels:%d;seqsize:%d;set:%d;rot:%d;trav:%d:otrav:%d";

		TreeStats stats = _tree.getStats();

		String lg = String.format(fmt, _treeType, _curSeqType, _curLevels,
				_sequence.size(), _curSet, stats.getRotations(),
				stats.getTraversals(), stats.getOtherTraversals());

		log.info(lg);
	}

	private void swap(int[] arr, int i, int j) {
		int t = arr[i];
		arr[i] = arr[j];
		arr[j] = t;
	}

}
