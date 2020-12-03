import torch
import torch.nn as nn
import torch.nn.functional as F


class ConvolutionalAE(nn.Module):

    def __init__(self, num_channels: int, num_filters: int, learning_rate: float):
        super(ConvolutionalAE, self).__init__()

        # Encoder
        self.conv1 = nn.Conv2d(num_channels, num_filters, 3, padding=1)
        self.conv2 = nn.Conv2d(num_filters, 4, 3, padding=1)
        self.pool = nn.MaxPool2d(2, 2)

        # Decoder
        self.t_conv1 = nn.ConvTranspose2d(4, num_filters, 2, stride=2)
        self.t_conv2 = nn.ConvTranspose2d(num_filters, num_channels, 2, stride=2)

        self.loss = nn.BCELoss()
        self.optimizer = torch.optim.Adam(self.parameters(), lr=learning_rate)

    def forward(self, x):
        x = F.relu(self.conv1(x))
        x = self.pool(x)
        x = F.relu(self.conv2(x))
        x = self.pool(x)
        x = F.relu(self.t_conv1(x))
        x = torch.sigmoid(self.t_conv2(x))
        return x
