#%%

import numpy as np
import torch
import torchvision
import torchvision.transforms as transforms
import matplotlib.pyplot as plt

from src.algorithms import ConvolutionalAEAlg

#%%

transform = transforms.Compose([
    transforms.RandomCrop(32, padding=4),
    transforms.RandomRotation(degrees=45),
    transforms.RandomHorizontalFlip(),
    transforms.ToTensor()
])

# Download the training and test datasets
train_data = torchvision.datasets.CIFAR10(root='data', train=True, download=True, transform=transform)
val_data = torchvision.datasets.CIFAR10(root='data', train=False, download=True, transform=transform)

# Prepare data loaders
train_loader = torch.utils.data.DataLoader(train_data, batch_size=32, num_workers=0)
test_loader = torch.utils.data.DataLoader(val_data, batch_size=32, num_workers=0)

labels = list(train_data.class_to_idx.keys())
num_channels = 3
num_classes = len(np.unique(train_data.targets))

model = ConvolutionalAEAlg(num_channels=num_channels, num_filters=5, learning_rate=0.001)

#%%

# test model
print("Test model")
X_out = []
y_out = []
with torch.no_grad():
    for X, y in test_loader:
        model.alg.eval()
        reconstructed = model.alg.forward(X.to(model.device))
        X_out += reconstructed
        y_out = np.concatenate([y_out, y])

#%%

# train model
max_epochs = 10
print("Train model for {} epochs".format(max_epochs))
model.partial_fit(X=train_loader, num_iterations=max_epochs)

#%%

# test model
print("Test model")
X_out = []
y_out = []
with torch.no_grad():
    for X, y in test_loader:
        print(y)
        model.alg.eval()
        reconstructed = model.alg.forward(X.to(model.device))
        X_out += reconstructed
        y_out = np.concatenate([y_out, y])

#%%

# plot data
fig, axes = plt.subplots(ncols=3, nrows=3, sharex="all", sharey="all", figsize=[5, 5])
for class_label in range(num_classes-1):
    ax = axes.flatten()[class_label]
    indices = [i for i in range(len(y_out)) if y_out[i] == class_label]
    selected_index = np.random.choice(indices)
    image = X_out[selected_index]  # unnormalize
    label = y_out[selected_index]
    ax.imshow(np.moveaxis(image.numpy(), 0, 2))
    ax.set_title(labels[class_label])
    ax.set_xticks([])
    ax.set_yticks([])
    ax.set_yticklabels([])
    ax.set_xticklabels([])
plt.tight_layout()
plt.show()
#%%


