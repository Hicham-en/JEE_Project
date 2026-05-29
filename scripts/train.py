#!/usr/bin/env python3
"""
Minimal training script for the NLP annotation platform.
Accepts CLI arguments: --dataset <path> --lr <float> --epochs <int> --batch-size <int>
Outputs dummy metrics to stdout for the platform to parse.
"""

import argparse
import json
import sys


def main():
    parser = argparse.ArgumentParser(description='NLP Training Script')
    parser.add_argument('--dataset', required=True, help='Path to dataset CSV')
    parser.add_argument('--lr', type=float, default=0.001, help='Learning rate')
    parser.add_argument('--epochs', type=int, default=10, help='Number of epochs')
    parser.add_argument('--batch-size', type=int, default=32, help='Batch size')
    args = parser.parse_args()

    print(f"Training started: dataset={args.dataset}, lr={args.lr}, epochs={args.epochs}, batch_size={args.batch_size}")
    sys.stdout.flush()

    # Simulate training progress
    for epoch in range(1, args.epochs + 1):
        loss = 0.5 / (1 + epoch * 0.1)
        print(f"Epoch {epoch}/{args.epochs} - loss: {loss:.4f} - accuracy: {0.5 + epoch * 0.04:.4f}")
        sys.stdout.flush()

    # Output final metrics in a format parsable by NlpMetricsParser
    final_accuracy = min(0.5 + args.epochs * 0.04, 0.95)
    f1_score = final_accuracy * 0.98
    print(f"\naccuracy: {final_accuracy:.4f}")
    print(f"f1_score: {f1_score:.4f}")
    print(f"confusion_matrix: [[{int(final_accuracy * 100)}, {int((1 - final_accuracy) * 50)}], [{int((1 - final_accuracy) * 50)}, {int(final_accuracy * 100)}]]")
    sys.stdout.flush()


if __name__ == '__main__':
    main()
