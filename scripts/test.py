#!/usr/bin/env python3
"""
Minimal test script for the NLP annotation platform.
Accepts CLI arguments: --dataset <path> [--run <training_run_id>]
Outputs dummy metrics to stdout for the platform to parse.
"""

import argparse
import sys


def main():
    parser = argparse.ArgumentParser(description='NLP Test Script')
    parser.add_argument('--dataset', required=True, help='Path to dataset CSV')
    parser.add_argument('--run', type=int, default=None, help='Training run ID to load model from')
    args = parser.parse_args()

    print(f"Testing started: dataset={args.dataset}, training_run_id={args.run}")
    sys.stdout.flush()

    # Simulate test evaluation
    print("Evaluating on test set...")
    sys.stdout.flush()

    accuracy = 0.87
    f1_score = 0.85
    print(f"\naccuracy: {accuracy:.4f}")
    print(f"f1_score: {f1_score:.4f}")
    print(f"confusion_matrix: [[87, 13], [15, 85]]")
    sys.stdout.flush()


if __name__ == '__main__':
    main()
